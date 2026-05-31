package cn.org.wang.exam.service.exam.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.mapper.*;
import cn.org.wang.exam.model.entity.*;
import cn.org.wang.exam.model.vo.record.ExamRecordDetailVO;
import cn.org.wang.exam.service.exam.ExamScoreService;
import cn.org.wang.exam.utils.SecurityUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 考试评分服务实现类
 * 处理评分、成绩查询操作
 *
 * @author Wang
 * @version 1.0
 */
/**
 * @deprecated 生产入口为 {@link cn.org.wang.exam.service.impl.ExamServiceImpl}
 */
@Deprecated
@Service
public class ExamScoreServiceImpl implements ExamScoreService {

    private static final Logger logger = LoggerFactory.getLogger(ExamScoreServiceImpl.class);

    @Resource
    private ExamMapper examMapper;
    @Resource
    private QuestionMapper questionMapper;
    @Resource
    private ExamQuestionMapper examQuestionMapper;
    @Resource
    private OptionMapper optionMapper;
    @Resource
    private ExamQuAnswerMapper examQuAnswerMapper;

    @Override
    public Result<List<ExamRecordDetailVO>> details(Integer examId) {
        Integer userId = SecurityUtil.getUserId();
        List<ExamRecordDetailVO> examRecordDetailVOS = new ArrayList<>();

        // 查询该考试的试题
        LambdaQueryWrapper<ExamQuestion> examQuestionWrapper = new LambdaQueryWrapper<>();
        examQuestionWrapper.eq(ExamQuestion::getExamId, examId)
                .orderByAsc(ExamQuestion::getSort);
        List<ExamQuestion> examQuestions = examQuestionMapper.selectList(examQuestionWrapper);

        if (examQuestions.isEmpty()) {
            return Result.success("查询成功", examRecordDetailVOS);
        }

        // 创建试题ID到分数的映射
        Map<Integer, Integer> questionScoreMap = examQuestions.stream()
                .collect(Collectors.toMap(ExamQuestion::getQuestionId, ExamQuestion::getScore));

        List<Integer> quIds = examQuestions.stream()
                .map(ExamQuestion::getQuestionId)
                .toList();

        // 批量查询题干列表
        List<Question> questions = questionMapper.selectBatchIds(quIds);

        // 批量查询选项（避免 N+1 查询）
        LambdaQueryWrapper<Option> optionBatchWrapper = new LambdaQueryWrapper<>();
        optionBatchWrapper.in(Option::getQuId, quIds);
        List<Option> allOptions = optionMapper.selectList(optionBatchWrapper);
        Map<Integer, List<Option>> optionMap = allOptions.stream()
                .collect(Collectors.groupingBy(Option::getQuId));

        // 批量查询用户答题记录（避免 N+1 查询）
        LambdaQueryWrapper<ExamQuAnswer> answerBatchWrapper = new LambdaQueryWrapper<>();
        answerBatchWrapper.eq(ExamQuAnswer::getUserId, userId)
                .eq(ExamQuAnswer::getExamId, examId)
                .in(ExamQuAnswer::getQuestionId, quIds);
        List<ExamQuAnswer> allAnswers = examQuAnswerMapper.selectList(answerBatchWrapper);
        Map<Integer, ExamQuAnswer> answerMap = allAnswers.stream()
                .collect(Collectors.toMap(ExamQuAnswer::getQuestionId, a -> a, (a1, a2) -> a1));

        for (Question temp : questions) {
            ExamRecordDetailVO examRecordDetailVO = new ExamRecordDetailVO();
            examRecordDetailVO.setImage(temp.getImage());
            examRecordDetailVO.setTitle(temp.getContent());
            examRecordDetailVO.setQuType(temp.getQuType());
            examRecordDetailVO.setAnalyse(temp.getAnalysis());
            examRecordDetailVO.setScore(questionScoreMap.getOrDefault(temp.getId(), 1));

            // 获取选项
            List<Option> options = optionMap.getOrDefault(temp.getId(), Collections.emptyList());
            if (temp.getQuType() == 4) {
                examRecordDetailVO.setOption(null);
            } else {
                examRecordDetailVO.setOption(options);
            }

            // 设置正确答案
            if (temp.getQuType() == 4 && !options.isEmpty()) {
                examRecordDetailVO.setRightOption(options.get(0).getContent());
            } else {
                List<Integer> correctSorts = options.stream()
                        .filter(op -> op.getIsRight() == 1)
                        .map(Option::getSort)
                        .toList();
                List<String> stringList = correctSorts.stream().map(String::valueOf).toList();
                examRecordDetailVO.setRightOption(String.join(",", stringList));
            }

            // 获取用户答案
            ExamQuAnswer userAnswer = answerMap.get(temp.getId());
            if (userAnswer != null) {
                processUserAnswer(userAnswer, temp.getQuType(), options, examRecordDetailVO);
            }

            examRecordDetailVOS.add(examRecordDetailVO);
        }

        return Result.success("查询成功", examRecordDetailVOS);
    }

    /**
     * 处理用户答案
     */
    private void processUserAnswer(ExamQuAnswer userAnswer, Integer quType, List<Option> options, ExamRecordDetailVO examRecordDetailVO) {
        switch (quType) {
            case 1:
            case 3:
                // 单选题和判断题
                options.stream()
                        .filter(op -> op.getId().toString().equals(userAnswer.getAnswerId()))
                        .findFirst()
                        .ifPresent(op -> examRecordDetailVO.setMyOption(Integer.toString(op.getSort())));
                examRecordDetailVO.setIsRight(userAnswer.getIsRight());
                break;
            case 2:
                // 多选题
                if (userAnswer.getAnswerId() != null) {
                    List<Integer> opIds = Arrays.stream(userAnswer.getAnswerId().split(","))
                            .map(Integer::parseInt)
                            .toList();
                    List<Integer> sorts = options.stream()
                            .filter(op -> opIds.contains(op.getId()))
                            .map(Option::getSort)
                            .toList();
                    List<String> shortList = sorts.stream().map(String::valueOf).toList();
                    examRecordDetailVO.setMyOption(String.join(",", shortList));
                }
                examRecordDetailVO.setIsRight(userAnswer.getIsRight());
                break;
            case 4:
                examRecordDetailVO.setMyOption(userAnswer.getAnswerContent());
                examRecordDetailVO.setIsRight(userAnswer.getIsRight());
                break;
            default:
                break;
        }
    }
}
