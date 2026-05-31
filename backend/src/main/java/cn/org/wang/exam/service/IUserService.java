package cn.org.wang.exam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.model.entity.User;
import cn.org.wang.exam.model.form.user.UserForm;
import cn.org.wang.exam.model.vo.user.UserVO;

import org.springframework.web.multipart.MultipartFile;

/**
 * 用户服务接口
 * 定义用户相关的业务逻辑方法
 * 包括用户信息管理、密码修改、课程加入等功能
 *
 * @author Wang
 * @since 2026-03-21
 */
public interface IUserService extends IService<User> {



    /**
     * 用户修改密码
     * 验证旧密码后更新为新密码
     *
     * @param userForm 用户表单，包含旧密码和新密码
     * @return 操作结果
     */
    Result<String> updatePassword(UserForm userForm);

    /**
     * 批量删除用户
     * 根据用户ID列表批量删除用户记录
     *
     * @param ids 用户ID字符串，多个ID用逗号分隔
     * @return 操作结果
     */
    Result<String> deleteBatchByIds(String ids);



    /**
     * 获取用户个人信息
     * 根据当前登录用户获取用户详细信息
     *
     * @return 用户信息VO对象
     */
    Result<UserVO> info();

    /**
     * 用户加入课程
     * 只有学生角色才能加入课程，通过课程代码加入
     *
     * @param code 课程代码
     * @return 操作结果
     */
    Result<String> joinsubject(String code);

    /**
     * 教师和管理员用户管理
     * 分页获取用户信息，支持按学号、姓名、注册日期、角色筛选
     *
     * @param pageNum    页码
     * @param pageSize   每页大小
     * @param userNo     学号/工号
     * @param realName   真实姓名
     * @param createDate 注册日期
     * @param roleId     角色ID
     * @return 分页用户信息
     */
    Result<IPage<UserVO>> pagingUser(Integer pageNum, Integer pageSize, String userNo, String realName, String createDate, Integer roleId);


    /**
     * 用户上传头像
     * 上传用户头像图片并返回图片访问地址
     *
     * @param file 头像文件
     * @return 图片访问地址
     */
    Result<String> uploadAvatar(MultipartFile file);

}
