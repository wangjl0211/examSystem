package cn.org.wang.exam.service.exam.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.org.wang.exam.common.exception.ServiceRuntimeException;
import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.constants.SystemConstants;
import cn.org.wang.exam.converter.ExamConverter;
import cn.org.wang.exam.mapper.*;
import cn.org.wang.exam.model.entity.*;
import cn.org.wang.exam.model.vo.exam.*;
import cn.org.wang.exam.service.IQuestionService;
import cn.org.wang.exam.service.exam.ExamQueryService;
import cn.org.wang.exam.utils.SecurityUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 考试查询服务实现类
 * 处理考试的查询操作
 *
 * @author Wang
 * @version 1.0
 */
/**
 * @deprecated 生产入口为 {@link cn.org.wang.exam.service.impl.ExamServiceImpl}
 */
@Deprecated
@Service
public class ExamQueryServiceImpl implements ExamQueryService {

    private static final Logger logger = LoggerFactory.getLogger(ExamQueryServiceImpl.class);

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
    private ExamQuAnswerMapper examQuAnswerMapper;
    @Resource
    private UserExamsScoreMapper userExamsScoreMapper;
    @Resource
    private ExamSubjectMapper examSubjectMapper;
    @Resource
    private UserMapper userMapper;

    @Override
    public Result<IPage<ExamVO>> getPagingExam(Integer pageNum, Integer pageSize, String title) {
        // 创建Page对象
        Page<Exam> page = new Page<>(pageNum, pageSize);
        // 开始查询
        LambdaQueryWrapper<Exam> examQuery = new LambdaQueryWrapper<>();
        examQuery.like(StringUtils.isNotBlank(title), Exam::getTitle, title)
                .eq(Exam::getIsDeleted, 0);
        if (SecurityUtil.getRoleCode() == SystemConstants.ROLE_TEACHER) {
            examQuery.eq(Exam::getUserId, SecurityUtil.getUserId());
        }
        Page<Exam> examPage = examMapper.selectPage(page, examQuery);
        // 实体转换
        Page<ExamVO> examVOPage = examConverter.pageEntityToVo(examPage);
        return Result.success("查询成功", examVOPage);
    }

    @Override
    public Result<ExamDetailVO> getDetail(Integer examId) {
        // 查询考试详情信息
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            return Result.failed("考试不存在");
        }
        // 实体转换
        ExamDetailVO examDetailVO = examConverter.examToExamDetailVO(exam);
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getId, examDetailVO.getUserId());
        User user = userMapper.selectOne(userLambdaQueryWrapper);
        if (user != null) {
            examDetailVO.setUsername(user.getRealName());
        }
        return Result.success("查询成功", examDetailVO);
    }

    @Override
    public Result<ExamQuestionListVO> getQuestionList(Integer examId) {
        try {
            Integer userId = SecurityUtil.getUserId();
            logger.info("=== 获取试题列表调试信息 ===");
            logger.info("用户ID: {}, 考试ID: {}", userId, examId);

            // 检查是否正在考试
            if (!isUserTakingExam(examId, userId)) {
                return Result.failed("没有正在进行的考试");
            }

            ExamQuestionListVO examQuestionListVO = new ExamQuestionListVO();

            // 获取考试基本信息
            Exam exam = examMapper.selectById(examId);
            if (exam == null) {
                return Result.failed("考试不存在");
            }

            // 设置考试时长
            examQuestionListVO.setExamDuration(exam.getExamDuration());

            // 计算并设置剩余时间
            long leftSeconds = calculateRemainingTime(examId, userId, exam.getExamDuration());
            examQuestionListVO.setLeftSeconds(leftSeconds);

            // 添加试题列表
            addQuestionsToList(examQuestionListVO, examId, userId);

            logger.info("=== 获取试题列表调试结束 ===");

            return Result.success("查询成功", examQuestionListVO);

        } catch (Exception e) {
            logger.error("获取试题列表异常: {}", e.getMessage(), e);
            return Result.failed("系统错误");
        }
    }

    @Override
    public Result<List<ExamQuCollectVO>> getCollect(Integer examId) {
        Integer userId = SecurityUtil.getUserId();
        // 检查是否正在考试
        if (!isUserTakingExam(examId, userId)) {
            return Result.failed("没有考试在进行");
        }
        List<ExamQuCollectVO> examQuCollectVOS = new ArrayList<>();
        // 查询该考试的试题
        LambdaQueryWrapper<ExamQuestion> examQuestionWrapper = new LambdaQueryWrapper<>();
        examQuestionWrapper.eq(ExamQuestion::getExamId, examId)
                .orderByAsc(ExamQuestion::getSort);
        List<ExamQuestion> examQuestions = examQuestionMapper.selectList(examQuestionWrapper);
        List<Integer> quIds = examQuestions.stream()
                .map(ExamQuestion::getQuestionId)
                .toList();

        if (quIds.isEmpty()) {
            return Result.success("查询成功", examQuCollectVOS);
        }

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
            ExamQuCollectVO examQuCollectVO = new ExamQuCollectVO();
            examQuCollectVO.setTitle(temp.getContent());
            examQuCollectVO.setQuType(temp.getQuType());
            examQuCollectVO.setId(temp.getId());

            // 获取选项
            List<Option> options = optionMap.getOrDefault(temp.getId(), Collections.emptyList());
            if (temp.getQuType() == 4) {
                examQuCollectVO.setOption(null);
            } else {
                examQuCollectVO.setOption(options);
            }

            // 获取用户答题记录
            ExamQuAnswer examQuAnswer = answerMap.get(temp.getId());
            if (examQuAnswer == null) {
                examQuCollectVO.setMyOption(null);
                examQuCollectVOS.add(examQuCollectVO);
                continue;
            }

            switch (temp.getQuType()) {
                case 1:
                case 3:
                    // 单选题和判断题
                    options.stream()
                            .filter(op -> op.getId().toString().equals(examQuAnswer.getAnswerId()))
                            .findFirst()
                            .ifPresent(op -> examQuCollectVO.setMyOption(Integer.toString(op.getSort())));
                    break;
                case 2:
                    // 多选题
                    List<Integer> opIds = Arrays.stream(examQuAnswer.getAnswerId().split(","))
                            .map(Integer::parseInt)
                            .toList();
                    List<Integer> sorts = options.stream()
                            .filter(op -> opIds.contains(op.getId()))
                            .map(Option::getSort)
                            .toList();
                    List<String> shortList = sorts.stream().map(String::valueOf).toList();
                    examQuCollectVO.setMyOption(String.join(",", shortList));
                    break;
                case 4:
                    examQuCollectVO.setMyOption(examQuAnswer.getAnswerContent());
                    break;
                default:
                    break;
            }
            examQuCollectVOS.add(examQuCollectVO);
        }
        return Result.success("查询成功", examQuCollectVOS);
    }

    @Override
    public Result<IPage<ExamsubjectListVO>> getsubjectExamList(Integer pageNum, Integer pageSize, String title, Boolean isASC) {
        IPage<ExamsubjectListVO> examPage = new Page<>(pageNum, pageSize);
        Integer userId = SecurityUtil.getUserId();
        String role = SecurityUtil.getRole();
        if ("role_student".equals(role)) {
            examSubjectMapper.selectClassExam(examPage, userId, title, isASC);
        } else if ("role_admin".equals(role)) {
            examSubjectMapper.selectAdminClassExam(examPage, userId, title, isASC);
        }
        return Result.success("查询成功", examPage);
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
     * 计算剩余时间
     */
    private long calculateRemainingTime(Integer examId, Integer userId, int examDuration) {
        LambdaQueryWrapper<UserExamsScore> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserExamsScore::getUserId, userId)
                .eq(UserExamsScore::getExamId, examId)
                .eq(UserExamsScore::getState, 0);
        UserExamsScore userExamsScore = userExamsScoreMapper.selectOne(wrapper);

        if (userExamsScore == null || userExamsScore.getCreateTime() == null) {
            throw new ServiceRuntimeException("无法获取考试开始时间");
        }

        LocalDateTime createTime = userExamsScore.getCreateTime();
        LocalDateTime endTime = createTime.plusMinutes(examDuration);
        LocalDateTime now = LocalDateTime.now();

        long leftSeconds = Duration.between(now, endTime).getSeconds();
        return Math.max(leftSeconds, 0);
    }

    /**
     * 添加试题到列表（优化 N+1 查询）
     */
    private void addQuestionsToList(ExamQuestionListVO examQuestionListVO, Integer examId, Integer userId) {
        for (Integer quType = 1; quType <= 4; quType++) {
            List<ExamQuestion> examQuestionList = examQuestionMapper.getExamQuByExamIdAndQuType(examId, quType);

            if (!examQuestionList.isEmpty()) {
                List<ExamQuestionVO> examQuestionVOS = examConverter.examQuestionListEntityToVO(examQuestionList);
                processExamQuestions(examQuestionVOS, examId, userId);
                setQuestionsByType(examQuestionListVO, quType, examQuestionVOS);
            }
        }
    }

    /**
     * 处理考试题目（批量查询优化）
     */
    private void processExamQuestions(List<ExamQuestionVO> examQuestionVOS, Integer examId, Integer userId) {
        if (examQuestionVOS.isEmpty()) {
            return;
        }

        List<Integer> questionIds = examQuestionVOS.stream()
                .map(ExamQuestionVO::getQuestionId)
                .toList();

        // 批量查询题目信息
        Map<Integer, Question> questionMap = questionService.listByIds(questionIds).stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        // 批量查询用户答题记录
        LambdaQueryWrapper<ExamQuAnswer> answerWrapper = new LambdaQueryWrapper<>();
        answerWrapper.eq(ExamQuAnswer::getExamId, examId)
                .eq(ExamQuAnswer::getUserId, userId)
                .in(ExamQuAnswer::getQuestionId, questionIds);
        Map<Integer, List<ExamQuAnswer>> answerMap = examQuAnswerMapper.selectList(answerWrapper).stream()
                .collect(Collectors.groupingBy(ExamQuAnswer::getQuestionId));

        // 批量查询选项
        LambdaQueryWrapper<Option> optionWrapper = new LambdaQueryWrapper<>();
        optionWrapper.in(Option::getQuId, questionIds).orderByAsc(Option::getSort);
        Map<Integer, List<Option>> optionMap = optionMapper.selectList(optionWrapper).stream()
                .collect(Collectors.groupingBy(Option::getQuId));

        for (ExamQuestionVO temp : examQuestionVOS) {
            temp.setCheckout(answerMap.containsKey(temp.getQuestionId()));
            Question question = questionMap.get(temp.getQuestionId());
            if (question != null) {
                temp.setContent(question.getContent());
                temp.setImage(question.getImage());
                temp.setOptions(examConverter.opListEntityToVO(
                        optionMap.getOrDefault(temp.getQuestionId(), Collections.emptyList())));
            }
        }
    }

    /**
     * 根据题型设置题目列表
     */
    private void setQuestionsByType(ExamQuestionListVO examQuestionListVO, Integer quType, List<ExamQuestionVO> examQuestionVOS) {
        switch (quType) {
            case 1 -> examQuestionListVO.setRadioList(examQuestionVOS);
            case 2 -> examQuestionListVO.setMultiList(examQuestionVOS);
            case 3 -> examQuestionListVO.setJudgeList(examQuestionVOS);
            case 4 -> examQuestionListVO.setSaqList(examQuestionVOS);
            default -> logger.warn("未知题型: {}", quType);
        }
    }
}
