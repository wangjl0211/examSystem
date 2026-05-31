package cn.org.wang.exam.service.exam.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

import cn.org.wang.exam.common.exception.ServiceRuntimeException;
import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.converter.ExamConverter;
import cn.org.wang.exam.converter.ExamQuAnswerConverter;
import cn.org.wang.exam.mapper.*;
import cn.org.wang.exam.model.entity.*;
import cn.org.wang.exam.model.form.exam_qu_answer.ExamQuAnswerAddForm;
import cn.org.wang.exam.model.vo.exam.ExamQuDetailVO;
import cn.org.wang.exam.model.vo.exam.OptionVO;
import cn.org.wang.exam.service.IOptionService;
import cn.org.wang.exam.service.IQuestionService;
import cn.org.wang.exam.model.enums.ExamSubmitSource;
import cn.org.wang.exam.service.exam.ExamAnswerService;
import cn.org.wang.exam.service.exam.ExamSubmissionService;
import cn.org.wang.exam.utils.SecurityUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 考试答题服务实现类
 * 处理答题、交卷操作
 * 交卷逻辑已统一至 {@link ExamSubmissionService}
 *
 * @author Wang
 * @version 1.0
 * @deprecated 生产入口仍为 {@link cn.org.wang.exam.service.impl.ExamServiceImpl}，本类供 Facade 渐进迁移使用
 */
@Deprecated
@Service
public class ExamAnswerServiceImpl implements ExamAnswerService {

    private static final Logger logger = LoggerFactory.getLogger(ExamAnswerServiceImpl.class);

    @Resource
    private ExamMapper examMapper;
    @Resource
    private ExamConverter examConverter;
    @Resource
    private QuestionMapper questionMapper;
    @Resource
    private IQuestionService questionService;
    @Resource
    private ExamQuestionMapper examQuestionMapper;
    @Resource
    private OptionMapper optionMapper;
    @Resource
    private IOptionService optionService;
    @Resource
    private ExamQuAnswerMapper examQuAnswerMapper;
    @Resource
    private UserExamsScoreMapper userExamsScoreMapper;
    @Resource
    private ExamQuAnswerConverter examQuAnswerConverter;
    @Resource
    private ExamSubmissionService examSubmissionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> startExam(Integer examId) {
        Integer userId = SecurityUtil.getUserId();

        LambdaQueryWrapper<UserExamsScore> ongoingThisExam = new LambdaQueryWrapper<>();
        ongoingThisExam.eq(UserExamsScore::getUserId, userId)
                .eq(UserExamsScore::getExamId, examId)
                .eq(UserExamsScore::getState, 0);
        if (userExamsScoreMapper.selectCount(ongoingThisExam) > 0) {
            return Result.success("继续考试");
        }

        LambdaQueryWrapper<UserExamsScore> otherOngoing = new LambdaQueryWrapper<>();
        otherOngoing.eq(UserExamsScore::getUserId, userId)
                .eq(UserExamsScore::getState, 0)
                .ne(UserExamsScore::getExamId, examId);
        UserExamsScore other = userExamsScoreMapper.selectOne(otherOngoing);
        if (other != null) {
            Exam otherExam = examMapper.selectById(other.getExamId());
            String title = otherExam != null ? otherExam.getTitle() : "其他考试";
            return Result.failed(title + "正在考试中");
        }

        LambdaQueryWrapper<UserExamsScore> submitted = new LambdaQueryWrapper<>();
        submitted.eq(UserExamsScore::getUserId, userId)
                .eq(UserExamsScore::getExamId, examId)
                .eq(UserExamsScore::getState, 1);
        if (userExamsScoreMapper.selectCount(submitted) > 0) {
            return Result.failed("这场考试已考不能第二次考试");
        }

        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            return Result.failed("考试不存在");
        }

        UserExamsScore userExamsScore = new UserExamsScore();
        userExamsScore.setExamId(examId);
        userExamsScore.setUserId(userId);
        userExamsScore.setTotalTime((long) exam.getExamDuration() * 60);
        userExamsScore.setState(0);

        int rows = userExamsScoreMapper.insert(userExamsScore);
        if (rows == 0) {
            return Result.failed("访问失败");
        }
        return Result.success("已开始考试");
    }

    @Override
    public Result<ExamQuDetailVO> getQuestionSingle(Integer examId, Integer quId) {
        Integer userId = SecurityUtil.getUserId();

        // 检查是否正在考试
        if (!isUserTakingExam(examId, userId)) {
            return Result.failed("没有考试在进行");
        }

        ExamQuDetailVO examQuDetailVO = new ExamQuDetailVO();

        // 查询 ExamQuestion 信息
        LambdaQueryWrapper<ExamQuestion> eqWrapper = new LambdaQueryWrapper<>();
        eqWrapper.eq(ExamQuestion::getQuestionId, quId)
                .eq(ExamQuestion::getExamId, examId);
        ExamQuestion examQuestion = examQuestionMapper.selectOne(eqWrapper);
        if (examQuestion != null) {
            examQuDetailVO.setSort(examQuestion.getSort());
        }

        // 查询 Question 信息
        Question question = questionService.getById(quId);
        if (question == null) {
            return Result.failed("题目不存在");
        }
        examQuDetailVO.setImage(question.getImage());
        examQuDetailVO.setContent(question.getContent());
        examQuDetailVO.setQuType(question.getQuType());

        // 处理选项和用户作答情况
        List<OptionVO> optionVOS = processOptionsAndUserAnswers(examId, quId, userId, examQuDetailVO);

        // 根据题目类型设置答案列表
        if (question.getQuType() != 4) {
            examQuDetailVO.setAnswerList(optionVOS);
        }

        return Result.success("获取成功", examQuDetailVO);
    }

    @Override
    public Result<String> addAnswer(ExamQuAnswerAddForm examQuAnswerForm) {
        Integer userId = SecurityUtil.getUserId();

        // 查询试题类型
        Question question = questionMapper.selectById(examQuAnswerForm.getQuId());
        if (question == null) {
            return Result.failed("题目不存在");
        }
        Integer quType = question.getQuType();

        // 查询是否有记录
        LambdaQueryWrapper<ExamQuAnswer> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(ExamQuAnswer::getExamId, examQuAnswerForm.getExamId())
                .eq(ExamQuAnswer::getQuestionId, examQuAnswerForm.getQuId())
                .eq(ExamQuAnswer::getUserId, userId);
        List<ExamQuAnswer> existingAnswers = examQuAnswerMapper.selectList(checkWrapper);

        if (!existingAnswers.isEmpty()) {
            return updateAnswerIfExists(examQuAnswerForm, quType, userId);
        } else {
            return insertNewAnswer(examQuAnswerForm, quType, userId);
        }
    }

    @Override
    public Result<ExamQuDetailVO> handExam(Integer examId) {
        return examSubmissionService.submitExam(examId, ExamSubmitSource.USER);
    }

    @Override
    public Result<Integer> addCheat(Integer examId) {
        Integer userId = SecurityUtil.getUserId();

        LambdaQueryWrapper<UserExamsScore> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserExamsScore::getExamId, examId)
                .eq(UserExamsScore::getUserId, userId);
        UserExamsScore userExamsScore = userExamsScoreMapper.selectOne(queryWrapper);

        if (userExamsScore == null) {
            return Result.failed("考试记录不存在");
        }

        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            return Result.failed("考试不存在");
        }

        int newCount = userExamsScore.getCount() + 1;

        // 达到最大切屏次数，触发强制交卷
        if (exam.getMaxCount() != null && newCount >= exam.getMaxCount()) {
            examSubmissionService.submitExam(examId, ExamSubmitSource.FORCE);
            return Result.success("切屏次数超过限制，已自动交卷", 1);
        }

        // 更新切屏次数
        LambdaUpdateWrapper<UserExamsScore> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserExamsScore::getExamId, examId)
                .eq(UserExamsScore::getUserId, userId)
                .set(UserExamsScore::getCount, newCount);
        userExamsScoreMapper.update(null, updateWrapper);

        return Result.success("提示：请勿切屏，已切屏次数:" + newCount + "，超过限制次数将强制交卷！", 0);
    }

    /**
     * 检查用户是否正在考试
     */
    private boolean isUserTakingExam(Integer examId, Integer userId) {
        LambdaQueryWrapper<UserExamsScore> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserExamsScore::getUserId, userId)
                .eq(UserExamsScore::getExamId, examId)
                .eq(UserExamsScore::getState, 0);
        return userExamsScoreMapper.selectCount(wrapper) > 0;
    }

    /**
     * 检查用户是否正在参加任何考试
     */
    private boolean isUserTakingAnyExam(Integer userId) {
        LambdaQueryWrapper<UserExamsScore> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserExamsScore::getUserId, userId)
                .eq(UserExamsScore::getState, 0);
        return userExamsScoreMapper.selectCount(wrapper) > 0;
    }

    /**
     * 获取正在进行的考试名称
     */
    private String getOngoingExamName(Integer userId) {
        LambdaQueryWrapper<UserExamsScore> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserExamsScore::getUserId, userId)
                .eq(UserExamsScore::getState, 0);
        UserExamsScore userExamsScore = userExamsScoreMapper.selectOne(wrapper);
        if (userExamsScore == null) {
            return null;
        }
        Exam exam = examMapper.selectById(userExamsScore.getExamId());
        return exam != null ? exam.getTitle() : null;
    }

    /**
     * 处理选项和用户作答情况
     */
    private List<OptionVO> processOptionsAndUserAnswers(Integer examId, Integer quId, Integer userId, ExamQuDetailVO examQuDetailVO) {
        LambdaQueryWrapper<Option> optionWrapper = new LambdaQueryWrapper<>();
        optionWrapper.eq(Option::getQuId, quId);
        List<Option> options = optionMapper.selectList(optionWrapper);
        List<OptionVO> optionVOS = examConverter.opListEntityToVO(options);

        // 查询用户作答记录
        LambdaQueryWrapper<ExamQuAnswer> answerWrapper = new LambdaQueryWrapper<>();
        answerWrapper.eq(ExamQuAnswer::getQuestionId, quId)
                .eq(ExamQuAnswer::getExamId, examId)
                .eq(ExamQuAnswer::getUserId, userId);
        List<ExamQuAnswer> examQuAnswers = examQuAnswerMapper.selectList(answerWrapper);

        if (!examQuAnswers.isEmpty()) {
            for (OptionVO optionVO : optionVOS) {
                for (ExamQuAnswer answer : examQuAnswers) {
                    processAnswerForOption(answer, optionVO, examQuDetailVO);
                }
            }
        }

        return optionVOS;
    }

    /**
     * 处理单个作答记录对选项的影响
     */
    private void processAnswerForOption(ExamQuAnswer answer, OptionVO optionVO, ExamQuDetailVO examQuDetailVO) {
        Integer questionType = answer.getQuestionType();
        String answerId = answer.getAnswerId();
        String answerContent = answer.getAnswerContent();
        String optionIdStr = optionVO.getId().toString();

        switch (questionType) {
            case 1, 3:
                optionVO.setCheckout(answerId.equals(optionIdStr));
                break;
            case 2:
                List<Integer> selectedOptionIds = Arrays.stream(answerId.split(","))
                        .map(Integer::parseInt)
                        .toList();
                optionVO.setCheckout(selectedOptionIds.contains(optionVO.getId()));
                break;
            case 4:
                optionVO.setContent(answerContent);
                examQuDetailVO.setAnswerList(Collections.singletonList(optionVO));
                break;
            default:
                break;
        }
    }

    /**
     * 插入新答案
     */
    private Result<String> insertNewAnswer(ExamQuAnswerAddForm form, Integer quType, Integer userId) {
        if (form.getAnswer() == null || form.getAnswer().trim().isEmpty()) {
            return Result.success("请求成功");
        }

        ExamQuAnswer examQuAnswer = prepareExamQuAnswer(form, quType, userId);

        switch (quType) {
            case 1:
                return insertSingleChoiceAnswer(form, examQuAnswer);
            case 2:
                return insertMultipleChoiceAnswer(form, examQuAnswer);
            case 3:
                return insertSingleChoiceAnswer(form, examQuAnswer);
            case 4:
                examQuAnswer.setIsRight(-1);
                examQuAnswerMapper.insert(examQuAnswer);
                return Result.success("请求成功");
            default:
                return Result.failed("请求错误，请联系管理员解决");
        }
    }

    /**
     * 更新已有答案
     */
    private Result<String> updateAnswerIfExists(ExamQuAnswerAddForm form, Integer quType, Integer userId) {
        if (form.getAnswer() == null || form.getAnswer().trim().isEmpty()) {
            LambdaUpdateWrapper<ExamQuAnswer> deleteWrapper = new LambdaUpdateWrapper<>();
            deleteWrapper.eq(ExamQuAnswer::getUserId, userId)
                    .eq(ExamQuAnswer::getExamId, form.getExamId())
                    .eq(ExamQuAnswer::getQuestionId, form.getQuId())
                    .set(ExamQuAnswer::getCheckout, false);
            examQuAnswerMapper.update(null, deleteWrapper);
            return Result.success("请求成功");
        }

        switch (quType) {
            case 1:
            case 3:
                return updateSingleChoiceAnswer(form, userId);
            case 2:
                return updateMultipleChoiceAnswer(form, userId);
            case 4:
                return updateShortAnswer(form, userId);
            default:
                return Result.failed("请求错误，请联系管理员解决");
        }
    }

    /**
     * 准备答题实体
     */
    private ExamQuAnswer prepareExamQuAnswer(ExamQuAnswerAddForm form, Integer quType, Integer userId) {
        ExamQuAnswer examQuAnswer = examQuAnswerConverter.formToEntity(form);
        if (quType == 4) {
            examQuAnswer.setAnswerContent(form.getAnswer());
        } else {
            examQuAnswer.setAnswerId(form.getAnswer());
        }
        examQuAnswer.setUserId(userId);
        examQuAnswer.setQuestionType(quType);
        return examQuAnswer;
    }

    /**
     * 插入单选题答案
     */
    private Result<String> insertSingleChoiceAnswer(ExamQuAnswerAddForm form, ExamQuAnswer answer) {
        Option option = optionService.getById(form.getAnswer());
        if (option == null) {
            return Result.failed("数据库中不存在该选项，请联系管理员解决");
        }
        answer.setIsRight(option.getIsRight() == 1 ? 1 : 0);
        examQuAnswerMapper.insert(answer);
        return Result.success("请求成功");
    }

    /**
     * 插入多选题答案
     */
    private Result<String> insertMultipleChoiceAnswer(ExamQuAnswerAddForm form, ExamQuAnswer answer) {
        List<Option> correctOptions = getCorrectOptions(form.getQuId());
        List<Integer> userOptionIds = parseAnswerIds(form.getAnswer());
        boolean isRight = isMultipleChoiceCorrect(correctOptions, userOptionIds);
        answer.setIsRight(isRight ? 1 : 0);
        examQuAnswerMapper.insert(answer);
        return Result.success("请求成功");
    }

    /**
     * 更新单选题答案
     */
    private Result<String> updateSingleChoiceAnswer(ExamQuAnswerAddForm form, Integer userId) {
        Option option = optionService.getById(form.getAnswer());
        if (option == null) {
            return Result.failed("数据库中不存在该试题，请联系管理员解决");
        }

        LambdaUpdateWrapper<ExamQuAnswer> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ExamQuAnswer::getUserId, userId)
                .eq(ExamQuAnswer::getExamId, form.getExamId())
                .eq(ExamQuAnswer::getQuestionId, form.getQuId())
                .set(ExamQuAnswer::getIsRight, option.getIsRight() == 1 ? 1 : 0)
                .set(ExamQuAnswer::getAnswerId, form.getAnswer())
                .set(ExamQuAnswer::getCheckout, true);
        examQuAnswerMapper.update(null, updateWrapper);
        return Result.success("请求成功");
    }

    /**
     * 更新多选题答案
     */
    private Result<String> updateMultipleChoiceAnswer(ExamQuAnswerAddForm form, Integer userId) {
        List<Option> correctOptions = getCorrectOptions(form.getQuId());
        if (correctOptions.isEmpty()) {
            return Result.failed("该题正确答案选项不存在");
        }

        List<Integer> userOptionIds = parseAnswerIds(form.getAnswer());
        boolean isRight = isMultipleChoiceCorrect(correctOptions, userOptionIds);

        LambdaUpdateWrapper<ExamQuAnswer> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ExamQuAnswer::getUserId, userId)
                .eq(ExamQuAnswer::getExamId, form.getExamId())
                .eq(ExamQuAnswer::getQuestionId, form.getQuId())
                .set(ExamQuAnswer::getAnswerId, form.getAnswer())
                .set(ExamQuAnswer::getIsRight, isRight ? 1 : 0)
                .set(ExamQuAnswer::getCheckout, true);
        examQuAnswerMapper.update(null, updateWrapper);
        return Result.success("请求成功");
    }

    /**
     * 更新简答题答案
     */
    private Result<String> updateShortAnswer(ExamQuAnswerAddForm form, Integer userId) {
        LambdaUpdateWrapper<ExamQuAnswer> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ExamQuAnswer::getUserId, userId)
                .eq(ExamQuAnswer::getExamId, form.getExamId())
                .eq(ExamQuAnswer::getQuestionId, form.getQuId())
                .set(ExamQuAnswer::getAnswerContent, form.getAnswer())
                .set(ExamQuAnswer::getCheckout, true);
        examQuAnswerMapper.update(null, updateWrapper);
        return Result.success("请求成功");
    }

    /**
     * 获取题目正确选项
     */
    private List<Option> getCorrectOptions(Integer quId) {
        LambdaQueryWrapper<Option> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Option::getIsRight, 1)
                .eq(Option::getQuId, quId);
        return optionMapper.selectList(wrapper);
    }

    /**
     * 解析答案ID字符串为列表
     */
    private List<Integer> parseAnswerIds(String answer) {
        return Arrays.stream(answer.split(","))
                .map(Integer::parseInt)
                .toList();
    }

    /**
     * 判断多选题答案是否正确
     */
    private boolean isMultipleChoiceCorrect(List<Option> correctOptions, List<Integer> userOptionIds) {
        for (Option option : correctOptions) {
            if (!userOptionIds.contains(option.getId())) {
                return false;
            }
        }
        return true;
    }
}
