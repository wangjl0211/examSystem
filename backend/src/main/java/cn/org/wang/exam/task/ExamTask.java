package cn.org.wang.exam.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.mapper.ExamMapper;
import cn.org.wang.exam.mapper.UserExamsScoreMapper;
import cn.org.wang.exam.model.entity.Exam;
import cn.org.wang.exam.model.entity.UserExamsScore;
import cn.org.wang.exam.model.enums.ExamState;
import cn.org.wang.exam.model.enums.ExamSubmitSource;
import cn.org.wang.exam.model.vo.exam.ExamQuDetailVO;
import cn.org.wang.exam.service.exam.ExamSubmissionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 考试定时任务：超时自动交卷
 */
@Component
@Slf4j
public class ExamTask {

    @Resource
    private UserExamsScoreMapper userExamsScoreMapper;
    @Resource
    private ExamMapper examMapper;
    @Resource
    private ExamSubmissionService examSubmissionService;

    /**
     * 定时检测超时未交卷记录并自动交卷
     */
    @Scheduled(initialDelay = 1000, fixedDelay = 5 * 1000)
    public void test() {
        LambdaQueryWrapper<UserExamsScore> query = new LambdaQueryWrapper<>();
        query.eq(UserExamsScore::getState, ExamState.ONGOING.getCode());
        List<UserExamsScore> userExamsScores = userExamsScoreMapper.selectList(query);
        LocalDateTime now = LocalDateTime.now();
        for (UserExamsScore userExamsScore : userExamsScores) {
            try {
                processUserExam(userExamsScore, now);
            } catch (Exception e) {
                log.error("自动交卷处理异常，用户考试记录ID: {}", userExamsScore.getId(), e);
            }
        }
    }

    private void processUserExam(UserExamsScore userExamsScore, LocalDateTime now) {
        Integer examId = userExamsScore.getExamId();
        Exam exam = examMapper.selectById(examId);

        if (exam == null) {
            log.error("考试不存在，examId: {}", examId);
            ((ExamTask) AopContext.currentProxy()).handExamForNonExistentExam(userExamsScore);
            return;
        }

        LocalDateTime userStartTime = userExamsScore.getCreateTime();
        if (userStartTime == null) {
            UserExamsScore currentRecord = userExamsScoreMapper.selectById(userExamsScore.getId());
            if (currentRecord == null || currentRecord.getCreateTime() == null) {
                log.error("无法获取用户考试记录的实际开始时间，用户考试记录ID: {}", userExamsScore.getId());
                return;
            }
            userStartTime = currentRecord.getCreateTime();
        }

        LocalDateTime userEndTime = userStartTime.plusMinutes(exam.getExamDuration());
        if (now.isAfter(userEndTime)) {
            Result<ExamQuDetailVO> result = examSubmissionService.submitExamForUser(
                    userExamsScore.getUserId(), examId, ExamSubmitSource.TIMEOUT);
            if (result.getCode() == 1) {
                log.info("自动交卷成功，用户ID: {}, 考试ID: {}", userExamsScore.getUserId(), examId);
            } else {
                log.warn("自动交卷失败，用户ID: {}, 考试ID: {}, 原因: {}",
                        userExamsScore.getUserId(), examId, result.getMsg());
            }
        }
    }

    /**
     * 考试不存在时结束进行中的考试记录
     */
    @Transactional(rollbackFor = Exception.class)
    public void handExamForNonExistentExam(UserExamsScore ues) {
        LocalDateTime nowTime = LocalDateTime.now();

        LambdaQueryWrapper<UserExamsScore> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(UserExamsScore::getUserId, ues.getUserId())
                .eq(UserExamsScore::getExamId, ues.getExamId())
                .eq(UserExamsScore::getState, 1);
        if (userExamsScoreMapper.selectOne(checkWrapper) != null) {
            return;
        }

        UserExamsScore userExamsScore = new UserExamsScore();
        userExamsScore.setUserScore(0);
        userExamsScore.setState(1);
        userExamsScore.setLimitTime(nowTime);
        if (ues.getCreateTime() != null) {
            userExamsScore.setUserTime(
                    java.time.Duration.between(ues.getCreateTime(), nowTime).getSeconds());
        }

        LambdaUpdateWrapper<UserExamsScore> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserExamsScore::getUserId, ues.getUserId())
                .eq(UserExamsScore::getExamId, ues.getExamId())
                .eq(UserExamsScore::getState, 0);
        userExamsScoreMapper.update(userExamsScore, updateWrapper);
    }
}
