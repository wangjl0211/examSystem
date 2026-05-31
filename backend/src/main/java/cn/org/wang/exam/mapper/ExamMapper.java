package cn.org.wang.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.org.wang.exam.model.entity.Exam;
import cn.org.wang.exam.model.vo.answer.AnswerExamVO;
import cn.org.wang.exam.model.vo.record.ExamRecordVO;

import org.apache.ibatis.annotations.Param;

/**
 * 试卷表 Mapper 接口
 *
 * @Author Wang
 * @since 2026-03-21
 */
public interface ExamMapper extends BaseMapper<Exam> {

    /**
     * 获取自己创建的考试，考试id，考试考试标题，是否需要阅卷
     *
     * @param userId 用户ID
     * @return 分页结果
     */
    IPage<AnswerExamVO> selectMarkedList(@Param("page") IPage<AnswerExamVO> page, @Param("userId") Integer userId, String role, String examName);

    /**
     * 查询学生的考试记录
     *
     * @param page     分页对象
     * @param userId   用户ID
     * @param examName 考试名称
     * @param isASC    是否升序
     * @return 分页结果
     */
    Page<ExamRecordVO> getExamRecordPage(Page<ExamRecordVO> page, Integer userId, String examName, Boolean isASC);

    /**
     * 查询教师创建的考试记录
     *
     * @param page     分页对象
     * @param userId   用户ID
     * @param examName 考试名称
     * @param isASC    是否升序
     * @return 分页结果
     */
    Page<ExamRecordVO> getTeacherExamRecordPage(Page<ExamRecordVO> page, Integer userId, String examName, Boolean isASC);

    /**
     * 查询所有考试记录（管理员）
     *
     * @param page     分页对象
     * @param examName 考试名称
     * @param isASC    是否升序
     * @return 分页结果
     */
    Page<ExamRecordVO> getAllExamRecordPage(Page<ExamRecordVO> page, String examName, Boolean isASC);

}
