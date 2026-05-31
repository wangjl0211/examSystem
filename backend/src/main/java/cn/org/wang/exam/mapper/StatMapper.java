package cn.org.wang.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import cn.org.wang.exam.model.entity.Subject;
import cn.org.wang.exam.model.vo.stat.SubjectExamVO;
import cn.org.wang.exam.model.vo.stat.SubjectStudentVO;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 统计数据 Mapper 接口
 *
 * @ Author JinXi
 * @ Version 1.0
 * @ Date 2026/5/12 14:56
 */
public interface StatMapper extends BaseMapper<Subject> {

    /**
     * 统计各班人数
     *
     * @return 人数总数
     */
    List<SubjectStudentVO> studentSubjectCount(@Param("roleId") Integer roleId, Integer id, List<Integer> subjectIdList);

    /**
     * 统计各班试卷数
     *
     * @return 试卷总数
     */
    List<SubjectExamVO> examSubjectCount(@Param("roleId") Integer roleId, Integer id, List<Integer> subjectIdList);

    /**
     * 统计试题类型分布
     * @param userId 用户ID (教师ID)
     * @return 试题类型统计
     */
    List<SubjectExamVO> questionTypeCount(@Param("userId") Integer userId);

}
