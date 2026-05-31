package cn.org.wang.exam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.mapper.ExamQuAnswerMapper;
import cn.org.wang.exam.model.entity.ExamQuAnswer;
import cn.org.wang.exam.model.vo.score.QuestionAnalyseVO;
import cn.org.wang.exam.service.IExamQuAnswerService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Wang
 * @since 2026-03-21
 */
@Service
public class ExamQuAnswerServiceImpl extends ServiceImpl<ExamQuAnswerMapper, ExamQuAnswer> implements IExamQuAnswerService {

    @Resource
    private ExamQuAnswerMapper examQuAnswerMapper;

    @Override
    public Result<QuestionAnalyseVO> questionAnalyse(Integer examId, Integer questionId) {
        QuestionAnalyseVO questionAnalyseVO = examQuAnswerMapper.questionAnalyse(examId, questionId);
        //正确率保留两位小数
        DecimalFormat format = new DecimalFormat("#.00");
        String strAccuracy = format.format(questionAnalyseVO.getRightCount() / questionAnalyseVO.getTotalCount());
        questionAnalyseVO.setAccuracy(Double.parseDouble(strAccuracy));
        return Result.success(null, questionAnalyseVO);
    }

}

