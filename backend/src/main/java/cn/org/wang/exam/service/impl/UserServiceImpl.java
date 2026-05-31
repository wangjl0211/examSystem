package cn.org.wang.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.org.wang.exam.common.exception.ServiceRuntimeException;
import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.converter.UserConverter;
import cn.org.wang.exam.mapper.AdminMapper;
import cn.org.wang.exam.mapper.SubjectMapper;
import cn.org.wang.exam.mapper.UserMapper;
import cn.org.wang.exam.mapper.UserSubjectMapper;
import cn.org.wang.exam.model.entity.Admin;
import cn.org.wang.exam.model.entity.Subject;
import cn.org.wang.exam.model.entity.User;
import cn.org.wang.exam.model.entity.UserSubject;
import cn.org.wang.exam.model.form.user.UserForm;
import cn.org.wang.exam.model.vo.user.UserVO;
import cn.org.wang.exam.service.IFileService;
import cn.org.wang.exam.service.IUserService;

import cn.org.wang.exam.utils.SecurityUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;


/**
 * 用户服务实现类
 *
 * @author Wang
 * @since 2026-03-21
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private static final String OLD_PASSWORD_ERROR = "旧密码错误";

    @Resource
    private UserMapper userMapper;
    @Resource
    private AdminMapper adminMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private HttpServletRequest request;
    @Resource
    private UserConverter userConverter;
    @Resource
    private SubjectMapper subjectMapper;
    @Resource
    private UserSubjectMapper userSubjectMapper;
    @Resource
    private IFileService fileService;




    @Override
    public Result<String> updatePassword(UserForm userForm) {
        Integer userId = SecurityUtil.getUserId();
        Integer roleCode = SecurityUtil.getRoleCode();
        
        if (!userForm.getNewPassword().equals(userForm.getCheckedPassword())) {
            throw new ServiceRuntimeException("两次密码不一致");
        }
        
        // 根据角色类型处理不同的密码更新逻辑
        if (roleCode == 0) {
            // 管理员用户，从t_admin表查询和更新
            Admin admin = adminMapper.selectById(userId);
            if (admin == null) {
                throw new ServiceRuntimeException("管理员用户不存在");
            }
            
            // 验证旧密码
            if (!new BCryptPasswordEncoder().matches(userForm.getOriginPassword(), admin.getPassword())) {
                throw new ServiceRuntimeException(OLD_PASSWORD_ERROR);
            }
            
            // 更新密码
            admin.setPassword(new BCryptPasswordEncoder().encode(userForm.getNewPassword()));
            int updated = adminMapper.updateById(admin);
            
            // 密码修改成功
            if (updated > 0) {
                return Result.success("修改成功，请重新登录");
            }
            throw new ServiceRuntimeException(OLD_PASSWORD_ERROR);
        } else {
            // 普通用户（教师/学生），从t_user表查询和更新
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new ServiceRuntimeException("用户不存在");
            }
            
            // 验证旧密码
            if (!new BCryptPasswordEncoder().matches(userForm.getOriginPassword(), user.getPassword())) {
                throw new ServiceRuntimeException(OLD_PASSWORD_ERROR);
            }
            
            // 设置新加密后的密码
            userForm.setPassword(new BCryptPasswordEncoder().encode(userForm.getNewPassword()));
            userForm.setId(userId);
            
            // 转换为User实体
            User updatedUser = userConverter.fromToEntity(userForm);
            
            // 调用mapper更新用户密码
            int updated = userMapper.updateById(updatedUser);
            
            // 密码修改成功
            if (updated > 0) {
                return Result.success("修改成功，请重新登录");
            }
            throw new ServiceRuntimeException(OLD_PASSWORD_ERROR);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> deleteBatchByIds(String ids) {
        List<Integer> userIds = Arrays.stream(ids.split(",")).map(Integer::parseInt).toList();
        List<Integer> adminList = userMapper.getAdminList();
        // 判断删除用户列表集合是否包含管理员列表中的id
        boolean containsAdminId = userIds.stream().anyMatch(adminList::contains);
        if(containsAdminId){
            throw new ServiceRuntimeException("无法删除管理员用户");
        }
        if (userIds.isEmpty()) {
            throw new ServiceRuntimeException("删除数据库时未传入用户Id");
        }
        Integer row = userMapper.deleteBatchIds(userIds);
        if (row < 1) {
            throw new ServiceRuntimeException("删除数据库时失败，条数<1");
        }
        return Result.success("删除成功");
    }



    /**
     * 获取用户个人信息
     *
     * @return
     */
    @Override
    public Result<UserVO> info() {
        // 获取用户信息
        Integer userId = SecurityUtil.getUserId();
        Integer roleCode = SecurityUtil.getRoleCode();
        
        UserVO userVo;
        if (roleCode == 0) { // 管理员角色
            // 从 t_admin 表查询管理员信息
            log.info("管理员用户获取信息，查询 t_admin 表");
            userVo = userMapper.getAdminInfo(userId);
        } else {
            // 从 t_user 表查询普通用户信息
            userVo = userMapper.info(userId);
        }
        
        return Result.success("获取用户信息成功", userVo);
    }

    /**
     * 用户加入课程，只有学生才能加入课程
     *
     * @param code
     * @return
     */
    @Override
    public Result<String> joinsubject(String code) {
        // 获取课程信息
        Integer userId = SecurityUtil.getUserId();
        LambdaQueryWrapper<Subject> wrapper = new LambdaQueryWrapper<Subject>().eq(Subject::getCode, code);
        Subject subject = subjectMapper.selectOne(wrapper);
        if (Objects.isNull(subject)) {
            throw new ServiceRuntimeException("课程口令不存在");
        }
        // 检查是否已经加入该课程
        LambdaQueryWrapper<UserSubject> userSubjectWrapper = new LambdaQueryWrapper<UserSubject>()
                .eq(UserSubject::getUId, userId)
                .eq(UserSubject::getGId, subject.getId());
        UserSubject existingUserSubject = userSubjectMapper.selectOne(userSubjectWrapper);
        if (existingUserSubject != null) {
            throw new ServiceRuntimeException("已经加入该课程");
        }
        // 添加用户与课程的关联
        UserSubject userSubject = new UserSubject();
        userSubject.setUId(userId);
        userSubject.setGId(subject.getId());
        userSubject.setIsDeleted(0);
        int inserted = userSubjectMapper.insert(userSubject);
        if (inserted > 0) {
            return Result.success("加入课程：" + subject.getSubjectName() + "成功");
        }
        throw new ServiceRuntimeException("加入课程失败,加入数据库时失败");
    }

    @Override
    public Result<IPage<UserVO>> pagingUser(Integer pageNum, Integer pageSize, String userNo, String realName, String createDate, Integer roleId) {
        IPage<UserVO> page = new Page<>(pageNum, pageSize);
        Integer userId = SecurityUtil.getUserId();
        Integer roleCode = SecurityUtil.getRoleCode();
        if (roleCode == 1) {
            // 教师查询所有用户
            page = userMapper.pagingUser(page, userNo, realName, createDate, roleId, userId, 1);
        } else {
            // 管理员直接查询所有用户
            page = userMapper.pagingUser(page, userNo, realName, createDate, roleId, userId, null);
        }
        return Result.success("分页获取用户信息成功", page);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result<String> uploadAvatar(MultipartFile file) {
        // 1.上传图片
        Integer userId = SecurityUtil.getUserId();
        Result<String> result = fileService.uploadImage(file);
        if (result.getCode() == 0) {
            throw new ServiceRuntimeException("图片上传失败,上传图片代码code为0");
        }
        // 2.设置数据库头像地址
        String url = result.getData();
        User user = new User();
        user.setId(userId);
        user.setAvatar(url);
        Integer row = userMapper.updateById(user);
        if (row > 0) {
            return Result.success("上传成功", url);
        }
        throw new ServiceRuntimeException("图片上传失败,修改用户表头像地址条数<=0");
    }


}

