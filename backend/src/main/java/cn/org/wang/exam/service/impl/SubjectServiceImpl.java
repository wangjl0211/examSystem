package cn.org.wang.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.org.wang.exam.common.exception.ServiceRuntimeException;
import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.converter.SubjectConverter;
import cn.org.wang.exam.mapper.DiscussionMapper;
import cn.org.wang.exam.mapper.SubjectMapper;
import cn.org.wang.exam.mapper.UserMapper;
import cn.org.wang.exam.mapper.UserSubjectMapper;
import cn.org.wang.exam.model.entity.Subject;
import cn.org.wang.exam.model.entity.UserSubject;
import cn.org.wang.exam.model.form.subject.SubjectForm;
import cn.org.wang.exam.model.vo.subject.SubjectUserVO;
import cn.org.wang.exam.model.vo.subject.SubjectVO;
import cn.org.wang.exam.service.ISubjectService;
import cn.org.wang.exam.utils.ClassTokenGenerator;
import cn.org.wang.exam.utils.SecurityUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 课程服务实现类
 *
 * @Author Wang
 * @since 2026-03-21
 */
@Service
public class SubjectServiceImpl extends ServiceImpl<SubjectMapper, Subject> implements ISubjectService {
    @Resource
    private SubjectMapper subjectMapper;
    @Resource
    private SubjectConverter subjectConverter;
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserSubjectMapper userSubjectMapper;
    @Resource
    private DiscussionMapper discussionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> addSubject(SubjectForm subjectForm) {
        // 生成课程口令
        String newCode = ClassTokenGenerator.generateClassToken(18);
        Integer userId = SecurityUtil.getUserId();
        String subjectName = subjectForm.getSubjectName();
        
        // 检查是否存在相同用户ID和课程名称的课程记录（包括逻辑删除的记录）
        Subject existingSubject = subjectMapper.getSubjectByUserAndName(userId, subjectName);
        
        if (existingSubject != null && existingSubject.getIsDeleted() != null && existingSubject.getIsDeleted() == 0) {
            // 课程已存在且未被删除，抛出异常
            throw new ServiceRuntimeException("该课程已存在");
        }
        
        if (existingSubject != null) {
            // 课程存在但被逻辑删除，恢复该课程
            int restoreRows = subjectMapper.restoreDeletedSubject(existingSubject.getId(), userId, newCode);
            if (restoreRows == 0) {
                throw new ServiceRuntimeException("恢复课程失败");
            }
            
            // 注意：根据需求，恢复的课程用户需要重新加入，所以不自动恢复或创建关联关系
            // 用户需要通过课程口令重新加入课程
            
            return Result.success("创建成功");
        }
        
        // 记录不存在，创建新课程
        subjectForm.setCode(newCode);
        // 实体转换
        Subject subject = subjectConverter.formToEntity(subjectForm);
        // 设置创建者ID
        subject.setUserId(userId);
        // 开始添加数据
        int rows = subjectMapper.insert(subject);
        if (rows == 0) {
            throw new ServiceRuntimeException("新建课程失败");
        }
        
        // 如果是教师创建课程，自动将教师加入该课程
        if (SecurityUtil.getRoleCode() == 1) {
            UserSubject userSubject = new UserSubject();
            userSubject.setGId(subject.getId());
            userSubject.setUId(userId);
            // 设置加入时间为课程创建时间，将LocalDateTime转换为Date
            userSubject.setJoinTime(subject.getCreateTime());
            userSubjectMapper.insert(userSubject);
        }
        
        return Result.success("创建成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateSubject(Integer id, SubjectForm subjectForm) {
        Integer userId = SecurityUtil.getUserId();
        String newSubjectName = subjectForm.getSubjectName();
        
        // 检查新的课程名称是否与当前用户创建的其他未删除课程重复
        Subject existingSubject = subjectMapper.getSubjectByUserAndName(userId, newSubjectName);
        
        if (existingSubject != null && existingSubject.getIsDeleted() != null && existingSubject.getIsDeleted() == 0 && !existingSubject.getId().equals(id)) {
            // 课程已存在且未被删除，且不是当前正在修改的课程，抛出异常
            throw new ServiceRuntimeException("该课程名称已存在");
        }
        // 如果课程存在但被逻辑删除，允许修改
        
        // 建立更新条件
        LambdaUpdateWrapper<Subject> subjectUpdateWrapper = new LambdaUpdateWrapper<>();
        subjectUpdateWrapper
                .set(Subject::getSubjectName, newSubjectName)
                .eq(Subject::getId, id);
        // 更新课程
        int rows = subjectMapper.update(subjectUpdateWrapper);
        if (rows == 0) {
            throw new ServiceRuntimeException("修改课程失败");
        }
        return Result.success("修改课程成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> deleteSubject(Integer subjectId) {
        // 逻辑删除课程
        int rows = subjectMapper.deleteById(subjectId);
        if (rows == 0) {
            throw new ServiceRuntimeException("删除课程失败");
        }
        
        // 级联删除该课程的所有讨论
        discussionMapper.deleteBySubjectId(subjectId);
        
        // 删除 t_user_subject 关联表数据（物理删除或逻辑删除取决于业务，这里建议物理删除关联）
        // 注意：MyBatis-Plus 的 deleteById 仅处理主键，关联表需要自定义删除
        LambdaUpdateWrapper<UserSubject> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserSubject::getGId, subjectId);
        userSubjectMapper.delete(wrapper);

        return Result.success("删除成功");
    }

    @Override
    public Result<IPage<SubjectVO>> getPaging(Integer pageNum, Integer pageSize, String subjectName, String userName, String createDate) {
        Page<SubjectVO> page = new Page<>(pageNum, pageSize);
        // 获取当前角色代码和用户ID
        Integer roleCode = SecurityUtil.getRoleCode();
        Integer userId = SecurityUtil.getUserId();
        // 如果是教师或学生获取课程ID列表
        List<Integer> subjectIdList = null;
        if (roleCode == 1) {
            // 教师角色：只获取自己创建的课程
            subjectIdList = subjectMapper.getSubjectIdListByCreatorId(userId);
        } else if (roleCode == 2) {
            // 学生角色：获取加入的课程
            subjectIdList = userSubjectMapper.getSubjectIdListByUserId(userId);
        }
        // 开始查询课程
        page = subjectMapper.selectSubjectPage(page, userId, subjectName, userName, createDate, roleCode, subjectIdList);
        return Result.success("查询成功", page);
    }

    @Override
    public Result<String> removeUserSubject(String ids) {
        // 字符串转换为列表
        List<Integer> userIds = Arrays.stream(ids.split(","))
                .map(Integer::parseInt)
                .toList();
        
        // 移出课程（使用MyBatis-Plus的delete方法，自动处理逻辑删除）
        int totalRows = 0;
        for (Integer userId : userIds) {
            LambdaUpdateWrapper<UserSubject> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(UserSubject::getUId, userId);
            totalRows += userSubjectMapper.delete(wrapper);
        }
        
        if (totalRows == 0) {
            throw new ServiceRuntimeException("批量用户移除课程失败");
        }
        return Result.success("批量用户移除课程成功");
    }

    @Override
    public Result<List<SubjectVO>> getAllSubject() {
        // 获取角色代码和用户ID
        Integer roleCode = SecurityUtil.getRoleCode();
        Integer userId = SecurityUtil.getUserId();
        List<Integer> subjectIdList = null;
        if (roleCode == 1) {
            // 教师角色：只获取自己创建的课程
            subjectIdList = subjectMapper.getSubjectIdListByCreatorId(userId);
            if (subjectIdList.isEmpty()) {
                throw new ServiceRuntimeException("未创建课程暂无数据");
            }
        } else if (roleCode == 2) {
            // 学生角色：获取加入的课程
            subjectIdList = userSubjectMapper.getSubjectIdListByUserId(userId);
        }
        // 开始查询当前用户管理的所有课程
        List<SubjectVO> subjects = subjectMapper.getAllSubject(userId, roleCode, subjectIdList);
        return Result.success("查询成功", subjects);
    }

    @Override
    public Result<String> joinSubject(String code) {
        // 获取课程信息 用户ID
        Subject subject = subjectMapper.getSubjectByCode(code);
        // 检查课程是否存在
        if (subject == null) {
            throw new ServiceRuntimeException("该课程不存在");
        }
        Integer userId = SecurityUtil.getUserId();
        Integer subjectId = subject.getId();
        
        // 检查用户与课程的关联记录是否存在（包括逻辑删除的记录）
        UserSubject existingUserSubject = userSubjectMapper.checkUserSubjectExists(userId, subjectId);
        
        if (existingUserSubject != null && existingUserSubject.getIsDeleted() != null && existingUserSubject.getIsDeleted() == 1) {
            // 记录被逻辑删除，使用专门的方法恢复记录
            int updateRows = userSubjectMapper.restoreUserSubject(existingUserSubject.getId(), java.time.LocalDateTime.now());
            if (updateRows > 0) {
                return Result.success("重新加入课程成功");
            } else {
                throw new ServiceRuntimeException("重新加入课程失败");
            }
        }
        
        if (existingUserSubject != null) {
            // 记录未被删除，用户已经在课程中
            throw new ServiceRuntimeException("您已经在该课程中");
        }
        
        // 记录不存在，创建新的关联记录
        UserSubject userSubject = new UserSubject();
        userSubject.setGId(subjectId);
        userSubject.setUId(userId);
        // 设置加入时间为当前时间
        userSubject.setJoinTime(java.time.LocalDateTime.now());
        // 开始添加学生和课程的联系
        int insert = userSubjectMapper.insert(userSubject);
        if (insert > 0) {
            return Result.success("加入课程成功");
        }
        throw new ServiceRuntimeException("加入课程失败");
    }

    @Override
    public Result<String> exitSubject(String subjectId) {
        // 获取用户ID
        Integer userId = SecurityUtil.getUserId();
        Integer subjectIdInt = Integer.parseInt(subjectId);
        
        // 使用MyBatis-Plus的delete方法，自动处理逻辑删除
        LambdaUpdateWrapper<UserSubject> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserSubject::getGId, subjectIdInt)
               .eq(UserSubject::getUId, userId);
        int rows = userSubjectMapper.delete(wrapper);
        
        if (rows > 0) {
            return Result.success("退出课程成功");
        }
        throw new ServiceRuntimeException("退出课程失败");
    }

    @Override
    public Result<String> userExitSubject() {
        // 获取课程和用户ID
        Integer subjectId = SecurityUtil.getSubjectId();
        Integer userId = SecurityUtil.getUserId();
        
        // 使用MyBatis-Plus的delete方法，自动处理逻辑删除
        LambdaUpdateWrapper<UserSubject> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserSubject::getGId, subjectId)
               .eq(UserSubject::getUId, userId);
        int rows = userSubjectMapper.delete(wrapper);
        
        if (rows > 0) {
            return Result.success("学生退出课程成功");
        }
        throw new ServiceRuntimeException("学生退出课程失败");
    }

    @Override
    public Result<Map<String, Object>> getSubjectDetail(Integer subjectId) {
        // 获取当前用户ID和角色
        Integer userId = SecurityUtil.getUserId();
        Integer roleCode = SecurityUtil.getRoleCode();
        
        // 获取课程信息
        Subject subject = subjectMapper.getSubjectById(subjectId);
        if (subject == null) {
            throw new ServiceRuntimeException("课程不存在");
        }
        
        // 权限检查：只有课程创建者和管理员可以查看课程详情
        if (roleCode != 3 && !subject.getUserId().equals(userId)) {
            throw new ServiceRuntimeException("无权限查看课程详情");
        }
        
        // 获取课程用户列表
        List<SubjectUserVO> userList = subjectMapper.getSubjectUsers(subjectId);
        
        // 过滤掉课程创建者信息
        List<SubjectUserVO> filteredUserList = userList.stream()
                .filter(user -> !user.getUserId().equals(subject.getUserId()))
                .toList();
        
        // 创建简化的课程信息对象，只包含课程名称
        Map<String, Object> simplifiedSubject = new HashMap<>();
        simplifiedSubject.put("id", subject.getId());
        simplifiedSubject.put("subjectName", subject.getSubjectName());
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("subject", simplifiedSubject);
        result.put("userList", filteredUserList);
        
        return Result.success("获取课程详情成功", result);
    }

    @Override
    public Result<String> removeUserFromSubject(Integer subjectId, Integer userId) {
        // 获取当前用户ID和角色
        Integer currentUserId = SecurityUtil.getUserId();
        Integer roleCode = SecurityUtil.getRoleCode();
        
        // 获取课程信息
        Subject subject = subjectMapper.getSubjectById(subjectId);
        if (subject == null) {
            throw new ServiceRuntimeException("课程不存在");
        }
        
        // 权限检查：只有课程创建者和管理员可以移除用户
        if (roleCode != 3 && !subject.getUserId().equals(currentUserId)) {
            throw new ServiceRuntimeException("无权限移除用户");
        }
        
        // 移除用户（使用MyBatis-Plus的delete方法，自动处理逻辑删除）
        LambdaUpdateWrapper<UserSubject> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserSubject::getGId, subjectId)
               .eq(UserSubject::getUId, userId);
        int rows = userSubjectMapper.delete(wrapper);
        
        if (rows == 0) {
            // 检查用户是否已经被移除
            LambdaUpdateWrapper<UserSubject> checkWrapper = new LambdaUpdateWrapper<>();
            checkWrapper.eq(UserSubject::getGId, subjectId)
                       .eq(UserSubject::getUId, userId)
                       .eq(UserSubject::getIsDeleted, 1);
            long deletedCount = userSubjectMapper.selectCount(checkWrapper);
            if (deletedCount > 0) {
                return Result.success("用户已被移除");
            }
            throw new ServiceRuntimeException("移除用户失败");
        }
        
        return Result.success("移除用户成功");
    }

}


