package cn.org.wang.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import cn.org.wang.exam.model.entity.UserSubject;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户课程关联Mapper接口
 *
 * @author Wang
 * @since 2026-03-21
 */
public interface UserSubjectMapper extends BaseMapper<UserSubject> {

    /**
     * 根据用户ID获取课程ID列表
     *
     * @param userId 用户ID
     * @return 课程ID列表
     */
    List<Integer> getSubjectIdListByUserId(Integer userId);

    /**
     * 删除用户课程关联
     *
     * @param userId 用户ID
     * @param subjectId 课程ID
     * @return 影响行数
     */
    Integer deleteUserSubject(Integer userId, Integer subjectId);

    /**
     * 根据课程ID获取用户ID列表
     *
     * @param subjectId 课程ID
     * @return 用户ID列表
     */
    List<Integer> getUserListBysubjectId(Integer subjectId);
    
    /**
     * 检查用户与课程的关联记录是否存在（包括逻辑删除的记录）
     *
     * @param userId 用户ID
     * @param subjectId 课程ID
     * @return 关联记录
     */
    UserSubject checkUserSubjectExists(Integer userId, Integer subjectId);
    
    /**
     * 恢复逻辑删除的用户课程关联记录
     *
     * @param id 记录ID
     * @param joinTime 新的加入时间
     * @return 影响行数
     */
    Integer restoreUserSubject(Integer id, LocalDateTime joinTime);

}