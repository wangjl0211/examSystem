package cn.org.wang.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import cn.org.wang.exam.model.entity.Examsubject;
import cn.org.wang.exam.model.vo.exam.ExamsubjectListVO;

import java.util.List;

/**
 * 考试与课程关联表 Mapper 接口
 *
 * @author Wang
 * @since 2026-03-21
 */
public interface ExamSubjectMapper extends BaseMapper<Examsubject> {

    /**
     * 添加试卷与课程的关联
     *
     * @param examId   试卷ID
     * @param subjectIds 课程ID集合
     * @return 添加记录数
     */
    Integer addExamsubject(Integer examId, List<Integer> subjectIds);

    /**
     * 根据开始id获取所有需要参加该考试的人数
     *
     * @param id 考试id
     * @return 人数
     */
    Integer selectClassSize(Integer id);

    /**
     * 查询考试课程关联列表
     *
     * @param examPage 分页page对象
     * @param userId   用户ID
     * @param title    标题
     * @param isASC    是否排序
     * @return 结果
     */
    IPage<ExamsubjectListVO> selectClassExam(IPage<ExamsubjectListVO> examPage, Integer userId, String title, Boolean isASC);

    /**
     * 获取管理员的试卷列表
     *
     * @param examPage 分页page对象
     * @param userId   用户ID
     * @param title    标题
     * @param isASC    是否排序
     * @return 结果
     */
    IPage<ExamsubjectListVO> selectAdminClassExam(IPage<ExamsubjectListVO> examPage, Integer userId, String title, Boolean isASC);

}