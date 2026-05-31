package cn.org.wang.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;

import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.model.entity.ExerciseRecord;
import cn.org.wang.exam.model.form.exercise.ExerciseFillAnswerFrom;
import cn.org.wang.exam.model.vo.exercise.AnswerInfoVO;
import cn.org.wang.exam.model.vo.exercise.QuestionSheetVO;
import cn.org.wang.exam.model.vo.question.QuestionVO;
import cn.org.wang.exam.model.vo.record.ExamRecordDetailWithTimeVO;

import java.util.List;

/**
 * 考试记录服务接口
 *
 * @author Wang
 * @since 2026-03-21
 */
public interface IExerciseRecordService extends IService<ExerciseRecord> {


    /**
     * 获取试题答题卡列表
     *
     * @param repoId 题库Id
     * @param quType 试题类型
     * @return 响应结果
     */
    Result<List<QuestionSheetVO>> getQuestionSheet(Integer repoId, Integer quType);

    /**
     * 查询某场考试的信息
     *
     * @param examId 试卷ID
     * @param userId
     * @return
     */
    Result<ExamRecordDetailWithTimeVO> getExamRecordDetail(Integer examId, Integer userId);




    /**
     * 填充答案，并返回试题信息
     *
     * @param exerciseFillAnswerFrom 请求参数
     * @return 响应结果
     */
    Result<QuestionVO> fillAnswer(ExerciseFillAnswerFrom exerciseFillAnswerFrom);

    /**
     * 获取单题，没有选项
     *
     * @param id 试题ID
     * @return
     */
    Result<QuestionVO> getSingle(Integer id);

    /**
     * 获取用户回答详情
     *
     * @param repoId 题库ID
     * @param quId   试题ID
     * @return
     */
    Result<AnswerInfoVO> getAnswerInfo(Integer repoId, Integer quId);

    /**
     * 清除用户在指定题库的刷题记录
     *
     * @param repoId 题库Id
     * @return 响应结果
     */
    Result<String> clearRecord(Integer repoId);
}
