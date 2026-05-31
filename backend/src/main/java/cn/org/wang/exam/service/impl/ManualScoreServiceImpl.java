package cn.org.wang.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.mapper.*;
import cn.org.wang.exam.model.entity.ExamQuAnswer;
import cn.org.wang.exam.model.entity.ManualScore;
import cn.org.wang.exam.model.entity.UserExamsScore;
import cn.org.wang.exam.model.form.answer.CorrectAnswerFrom;
import cn.org.wang.exam.model.vo.answer.AnswerExamVO;
import cn.org.wang.exam.model.vo.answer.UncorrectedUserVO;
import cn.org.wang.exam.model.vo.answer.UserAnswerDetailVO;
import cn.org.wang.exam.service.IManualScoreService;
import cn.org.wang.exam.utils.SecurityUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 答卷管理服务实现类
 *
 * @author Wang
 * @since 2026-03-21
 */
@Slf4j
@Service
public class ManualScoreServiceImpl extends ServiceImpl<ManualScoreMapper, ManualScore> implements IManualScoreService {

    @Resource
    private ExamMapper examMapper;
    @Resource
    private ExamSubjectMapper examSubjectMapper;
    @Resource
    private UserExamsScoreMapper userExamsScoreMapper;
    @Resource
    private ExamQuAnswerMapper examQuAnswerMapper;
    @Resource
    private ManualScoreMapper manualScoreMapper;

    /**
     * 试卷查询信息
     *
     * @param userId
     * @param examId
     * @return
     */
    @Override
    public Result<List<UserAnswerDetailVO>> getDetail(Integer userId, Integer examId) {
        List<UserAnswerDetailVO> list = examQuAnswerMapper.selectUserAnswer(userId, examId);
        return Result.success("查询成功", list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> correct(List<CorrectAnswerFrom> correctAnswerFroms) {
        List<ManualScore> list = new ArrayList<>(correctAnswerFroms.size());
        AtomicInteger manualTotalScore = new AtomicInteger();
        correctAnswerFroms.forEach(correctAnswerFrom -> {

            // 获取用户作答信息id - 修复P0 NPE风险：添加null检查
            LambdaQueryWrapper<ExamQuAnswer> wrapper = new LambdaQueryWrapper<ExamQuAnswer>()
                    .select(ExamQuAnswer::getId)
                    .eq(ExamQuAnswer::getExamId, correctAnswerFrom.getExamId())
                    .eq(ExamQuAnswer::getUserId, correctAnswerFrom.getUserId())
                    .eq(ExamQuAnswer::getQuestionId, correctAnswerFrom.getQuestionId());

            ExamQuAnswer examQuAnswer = examQuAnswerMapper.selectOne(wrapper);
            if (examQuAnswer == null) {
                log.warn("未找到用户作答记录，userId={}, examId={}, questionId={}", 
                    correctAnswerFrom.getUserId(), correctAnswerFrom.getExamId(), correctAnswerFrom.getQuestionId());
                return;
            }
            
            ManualScore manualScore = new ManualScore();
            manualScore.setExamQuAnswerId(examQuAnswer.getId());
            manualScore.setScore(correctAnswerFrom.getScore());
            manualScore.setUserId(SecurityUtil.getUserId());
            manualScore.setCreateTime(LocalDateTime.now());
            list.add(manualScore);
            manualTotalScore.addAndGet(correctAnswerFrom.getScore());
        });
        manualScoreMapper.insertList(list);

        int addScore = manualTotalScore.get();
        if (addScore < 0 || addScore > 10000) {
            return Result.failed("批改分数超出允许范围");
        }

        CorrectAnswerFrom correctAnswerFrom = correctAnswerFroms.get(0);
        LambdaQueryWrapper<UserExamsScore> scoreQuery = new LambdaQueryWrapper<>();
        scoreQuery.eq(UserExamsScore::getExamId, correctAnswerFrom.getExamId())
                .eq(UserExamsScore::getUserId, correctAnswerFrom.getUserId());
        UserExamsScore scoreRecord = userExamsScoreMapper.selectOne(scoreQuery);
        if (scoreRecord == null) {
            return Result.failed("用户考试记录不存在");
        }
        int baseScore = scoreRecord.getUserScore() != null ? scoreRecord.getUserScore() : 0;

        LambdaUpdateWrapper<UserExamsScore> userExamsScoreLambdaUpdateWrapper = new LambdaUpdateWrapper<UserExamsScore>()
                .eq(UserExamsScore::getExamId, correctAnswerFrom.getExamId())
                .eq(UserExamsScore::getUserId, correctAnswerFrom.getUserId())
                .set(UserExamsScore::getWhetherMark, 1)
                .set(UserExamsScore::getUserScore, baseScore + addScore);
        userExamsScoreMapper.update(userExamsScoreLambdaUpdateWrapper);

        return Result.success("批改成功");
    }

    @Override
    public Result<IPage<AnswerExamVO>> examPage(Integer pageNum, Integer pageSize, String examName) {

        Page<AnswerExamVO> page = new Page<>(pageNum, pageSize);
        // 获取自己创建的考试
        List<AnswerExamVO> list = examMapper.selectMarkedList(page, SecurityUtil.getUserId(), SecurityUtil.getRole(), examName).getRecords();

        // 获取相关信息
        list.forEach(answerExamVO -> {
            // 需要参加考试人数
            answerExamVO.setClassSize(examSubjectMapper.selectClassSize(answerExamVO.getExamId()));
            // 实际参加考试人数
            LambdaQueryWrapper<UserExamsScore> numberWrapper = new LambdaQueryWrapper<UserExamsScore>()
                    .eq(UserExamsScore::getExamId, answerExamVO.getExamId());
            answerExamVO.setNumberOfApplicants(userExamsScoreMapper.selectCount(numberWrapper).intValue());
            // 已阅人数
            LambdaQueryWrapper<UserExamsScore> correctedWrapper = new LambdaQueryWrapper<UserExamsScore>()
                    .eq(UserExamsScore::getWhetherMark, 1)
                    .eq(UserExamsScore::getExamId, answerExamVO.getExamId());
            answerExamVO.setCorrectedPaper(userExamsScoreMapper.selectCount(correctedWrapper).intValue());
        });
        return Result.success(null, page);

    }

    @Override

    public Result<IPage<UncorrectedUserVO>> stuExamPage(Integer pageNum, Integer pageSize, Integer examId, String realName) {
        IPage<UncorrectedUserVO> page = new Page<>(pageNum, pageSize);
        page = userExamsScoreMapper.uncorrectedUser(page, examId, realName);
        return Result.success(null, page);
    }
}

