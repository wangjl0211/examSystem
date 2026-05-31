package cn.org.wang.exam.service.exam;

import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.model.form.exam_qu_answer.ExamQuAnswerAddForm;
import cn.org.wang.exam.model.vo.exam.ExamQuDetailVO;

/**
 * 考试答题服务接口
 * 处理答题、交卷操作
 *
 * @author Wang
 * @version 1.0
 */
public interface ExamAnswerService {

    /**
     * 开始考试
     *
     * @param examId 考试ID
     * @return 操作结果
     */
    Result<String> startExam(Integer examId);

    /**
     * 获取单个题目详情
     *
     * @param examId 考试ID
     * @param quId 题目ID
     * @return 题目详情
     */
    Result<ExamQuDetailVO> getQuestionSingle(Integer examId, Integer quId);

    /**
     * 保存答案
     *
     * @param examQuAnswerForm 答案表单
     * @return 操作结果
     */
    Result<String> addAnswer(ExamQuAnswerAddForm examQuAnswerForm);

    /**
     * 提交试卷
     *
     * @param examId 考试ID
     * @return 操作结果
     */
    Result<ExamQuDetailVO> handExam(Integer examId);

    /**
     * 记录切屏作弊
     *
     * @param examId 考试ID
     * @return 操作结果
     */
    Result<Integer> addCheat(Integer examId);
}
