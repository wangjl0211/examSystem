package cn.org.wang.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.org.wang.exam.model.entity.Subject;
import cn.org.wang.exam.model.vo.subject.SubjectUserVO;
import cn.org.wang.exam.model.vo.subject.SubjectVO;

import java.util.List;

/**
 * 课程表 Mapper 接口
 *
 * @author Wang
 * @since 2026-03-21
 */
public interface SubjectMapper extends BaseMapper<Subject> {

    /**
     * 分页查找课程
     *
     * @param page        分页对象
     * @param userId      用户ID
     * @param subjectName   课程名称
     * @param userName      创建用户姓名
     * @param createDate    创建日期
     * @param roleCode    角色代码
     * @param subjectIdList 课程ID列表
     * @return 分页结果
     */
    Page<SubjectVO> selectSubjectPage(Page<SubjectVO> page, Integer userId, String subjectName, String userName, String createDate, Integer roleCode, List<Integer> subjectIdList);

    /**
     * 获得所有课程
     *
     * @param userId      用户ID
     * @param roleCode    角色代码
     * @param subjectIdList 课程ID列表
     * @return 结果集
     */
    List<SubjectVO> getAllSubject(Integer userId, Integer roleCode, List<Integer> subjectIdList);

    /**
     * 根据课程代码获取课程对象
     *
     * @param code 课程代码
     * @return 课程对象
     */
    Subject getSubjectByCode(String code);

    /**
     * 根据ID获取课程
     *
     * @param id 课程ID
     * @return 课程
     */
    Subject getSubjectById(Integer id);

    /**
     * 获取课程用户列表
     *
     * @param subjectId 课程ID
     * @return 课程用户列表
     */
    List<SubjectUserVO> getSubjectUsers(Integer subjectId);
    
    /**
     * 根据用户ID和课程名称查询课程记录（包括逻辑删除的记录）
     *
     * @param userId 用户ID
     * @param subjectName 课程名称
     * @return 课程对象
     */
    Subject getSubjectByUserAndName(Integer userId, String subjectName);
    
    /**
     * 恢复逻辑删除的课程
     *
     * @param id 课程ID
     * @param userId 创建者ID
     * @param code 课程代码
     * @return 影响行数
     */
    Integer restoreDeletedSubject(Integer id, Integer userId, String code);
    
    /**
     * 根据创建者ID获取课程ID列表
     *
     * @param creatorId 创建者ID
     * @return 课程ID列表
     */
    List<Integer> getSubjectIdListByCreatorId(Integer creatorId);

}
