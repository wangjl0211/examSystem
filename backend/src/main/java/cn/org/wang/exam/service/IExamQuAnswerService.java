package cn.org.wang.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;

import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.model.entity.ExamQuAnswer;
import cn.org.wang.exam.model.vo.score.QuestionAnalyseVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Wang
 * @since 2026-03-21
 */
public interface IExamQuAnswerService extends IService<ExamQuAnswer> {

    /**
     * 获取某场考试某题作答情况
     * @param examId 考试id
     * @param questionId 试题id
     * @return 结果
     */
    Result<QuestionAnalyseVO> questionAnalyse(Integer examId, Integer questionId);

}
