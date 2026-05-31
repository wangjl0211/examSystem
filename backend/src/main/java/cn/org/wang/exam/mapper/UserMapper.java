package cn.org.wang.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import cn.org.wang.exam.model.entity.User;
import cn.org.wang.exam.model.vo.user.UserVO;

import java.util.List;

/**
 * 用户服务Mapper
 *
 * @author Wang
 * @since 2026-03-21
 */
public interface UserMapper extends BaseMapper<User> {

    /**
     * 移除教师和课程的关联
     *
     * @param userIds 用户列表
     * @return 返回修改条数
     */
    Integer removeUsersubject(List<Integer> userIds);

    /**
     * 批量添加用户
     *
     * @param list 用户id列表
     * @return 返回删除条数
     */
    Integer insertBatchUser(List<User> list);

    /**
     * 获取用户信息
     *
     * @param userId 用户ID
     * @return 返回UserVO
     */
    UserVO info(Integer userId);
    
    /**
     * 获取管理员信息
     *
     * @param userId 用户ID
     * @return 返回UserVO
     */
    UserVO getAdminInfo(Integer userId);

    /**
     * 分页获取用户信息
     *
     * @param page        分页信息
     * @param userNo      学号/工号
     * @param realName    真实姓名
     * @param createDate  注册日期
     * @param roleId      角色Id
     * @param userId      用户Id
     * @param queryRoleId 查询角色Id
     * @return 查询结果集
     */
    IPage<UserVO> pagingUser(IPage<UserVO> page, String userNo, String realName, String createDate, Integer roleId, Integer userId, Integer queryRoleId);

    /**
     * 用户退出课程
     *
     * @param subjectId 课程ID
     * @param userId  用户ID
     * @return 返回更新条数
     */
    Integer userExitsubject(Integer subjectId, Integer userId);

    /**
     * 获得管理员ID列表
     *
     * @return 返回管理员id列表
     */
    List<Integer> getAdminList();
    
    /**
     * 查询指定前缀的最大用户编号，包括逻辑删除的记录
     *
     * @param prefix 用户编号前缀
     * @param roleId 角色ID
     * @return 返回最大用户编号的用户
     */
    User selectMaxUserNo(String prefix, Integer roleId);

}
