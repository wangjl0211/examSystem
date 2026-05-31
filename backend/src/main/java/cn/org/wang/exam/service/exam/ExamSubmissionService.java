package cn.org.wang.exam.service.exam;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.mapper.ExamMapper;
import cn.org.wang.exam.mapper.ExamQuAnswerMapper;
import cn.org.wang.exam.mapper.ExamQuestionMapper;
import cn.org.wang.exam.mapper.UserExamsScoreMapper;
import cn.org.wang.exam.model.entity.Exam;
import cn.org.wang.exam.model.entity.ExamQuAnswer;
import cn.org.wang.exam.model.entity.ExamQuestion;
import cn.org.wang.exam.model.entity.UserExamsScore;
import cn.org.wang.exam.model.enums.ExamSubmitSource;
import cn.org.wang.exam.model.vo.exam.ExamQuDetailVO;
import cn.org.wang.exam.utils.SecurityUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 统一交卷与客观分计算服务
 * 供 API 交卷、定时任务自动交卷、强制交卷共用，避免计分逻辑分叉
 */
@Slf4j
@Service
public class ExamSubmissionService {

    private static final String NO_ONGOING_EXAM = "没有考试在进行";

    @Resource
    private ExamMapper examMapper;
    @Resource
    private ExamQuAnswerMapper examQuAnswerMapper;
    @Resource
    private ExamQuestionMapper examQuestionMapper;
    @Resource
    private UserExamsScoreMapper userExamsScoreMapper;

    /**
     * 当前登录用户交卷（API）
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<ExamQuDetailVO> submitExam(Integer examId, ExamSubmitSource source) {
        Integer userId = SecurityUtil.getUserId();
        return submitExamForUser(userId, examId, source);
    }

    /**
     * 指定用户交卷（定时任务等）
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<ExamQuDetailVO> submitExamForUser(Integer userId, Integer examId, ExamSubmitSource source) {
        UserExamsScore ongoing = getOngoingRecord(userId, examId);
        if (ongoing == null) {
            if (isAlreadySubmitted(userId, examId)) {
                return Result.success("考试已提交");
            }
            return Result.failed(NO_ONGOING_EXAM);
        }

        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            return Result.failed("考试不存在: " + examId);
        }

        if (ongoing.getCreateTime() == null) {
            return Result.failed("交卷失败，无法确定考试开始时间或状态异常。");
        }

        LocalDateTime nowTime = LocalDateTime.now();
        if (source == ExamSubmitSource.USER
                && isAfterEndTime(ongoing.getCreateTime(), nowTime, exam.getExamDuration())) {
            return Result.failed("提交失败，已过交卷时间");
        }
        // TIMEOUT / FORCE 来源允许在截止时间后完成交卷（自动交卷、切屏强制交卷）

        handleUnansweredSaqQuestions(examId, userId);
        int calculatedScore = calculateObjectiveScore(examId, userId, exam);
        Long userTime = Duration.between(ongoing.getCreateTime(), nowTime).getSeconds();
        int whetherMark = determineWhetherMark(exam);

        int updateRows = updateUserExamScore(userId, examId, calculatedScore, nowTime, userTime, whetherMark);
        if (updateRows == 0) {
            if (isAlreadySubmitted(userId, examId)) {
                return Result.success("考试已提交");
            }
            return Result.failed("交卷失败，更新记录时发生未知错误。");
        }

        if (whetherMark == 0) {
            return Result.success("提交成功，待教师阅卷");
        }
        return Result.success("交卷成功");
    }

    private UserExamsScore getOngoingRecord(Integer userId, Integer examId) {
        LambdaQueryWrapper<UserExamsScore> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserExamsScore::getUserId, userId)
                .eq(UserExamsScore::getExamId, examId)
                .eq(UserExamsScore::getState, 0);
        return userExamsScoreMapper.selectOne(wrapper);
    }

    private boolean isAlreadySubmitted(Integer userId, Integer examId) {
        LambdaQueryWrapper<UserExamsScore> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserExamsScore::getUserId, userId)
                .eq(UserExamsScore::getExamId, examId)
                .eq(UserExamsScore::getState, 1);
        return userExamsScoreMapper.selectCount(wrapper) > 0;
    }

    private boolean isAfterEndTime(LocalDateTime startTime, LocalDateTime nowTime, Integer examDuration) {
        if (examDuration == null) {
            return false;
        }
        return nowTime.isAfter(startTime.plusMinutes(examDuration));
    }

    /**
     * 处理未作答的简答题（统一 isRight=0）
     */
    private void handleUnansweredSaqQuestions(Integer examId, Integer userId) {
        List<ExamQuestion> unanswered = examQuestionMapper.getUnansweredSaqQuestions(examId, userId);
        if (unanswered == null || unanswered.isEmpty()) {
            return;
        }
        for (ExamQuestion question : unanswered) {
            ExamQuAnswer examQuAnswer = new ExamQuAnswer();
            examQuAnswer.setExamId(examId);
            examQuAnswer.setUserId(userId);
            examQuAnswer.setQuestionId(question.getQuestionId());
            examQuAnswer.setQuestionType(4);
            examQuAnswer.setAnswerContent("");
            examQuAnswer.setIsRight(0);
            examQuAnswerMapper.insert(examQuAnswer);
        }
    }

    private int calculateObjectiveScore(Integer examId, Integer userId, Exam exam) {
        LambdaQueryWrapper<ExamQuAnswer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamQuAnswer::getUserId, userId)
                .eq(ExamQuAnswer::getExamId, examId);
        List<ExamQuAnswer> answers = examQuAnswerMapper.selectList(wrapper);

        int calculatedScore = 0;
        for (ExamQuAnswer answer : answers) {
            if (answer.getIsRight() != null && answer.getIsRight() == 1) {
                Integer questionType = answer.getQuestionType();
                if (questionType != null) {
                    calculatedScore += getScoreForQuestionType(questionType, exam);
                }
            }
        }
        return calculatedScore;
    }

    private int getScoreForQuestionType(Integer questionType, Exam exam) {
        return switch (questionType) {
            case 1 -> exam.getRadioScore() != null ? exam.getRadioScore() : 0;
            case 2 -> exam.getMultiScore() != null ? exam.getMultiScore() : 0;
            case 3 -> exam.getJudgeScore() != null ? exam.getJudgeScore() : 0;
            default -> 0;
        };
    }

    private int determineWhetherMark(Exam exam) {
        if (exam.getSaqCount() != null && exam.getSaqCount() > 0) {
            return 0;
        }
        return -1;
    }

    private int updateUserExamScore(Integer userId, Integer examId, int score,
                                    LocalDateTime submitTime, Long userTime, int whetherMark) {
        LambdaUpdateWrapper<UserExamsScore> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserExamsScore::getUserId, userId)
                .eq(UserExamsScore::getExamId, examId)
                .eq(UserExamsScore::getState, 0)
                .set(UserExamsScore::getUserScore, score)
                .set(UserExamsScore::getState, 1)
                .set(UserExamsScore::getLimitTime, submitTime)
                .set(UserExamsScore::getUserTime, userTime)
                .set(UserExamsScore::getWhetherMark, whetherMark);
        return userExamsScoreMapper.update(null, wrapper);
    }
}
