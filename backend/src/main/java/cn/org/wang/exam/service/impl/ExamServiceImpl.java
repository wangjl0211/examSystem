package cn.org.wang.exam.service.impl;

import com.aliyun.oss.ServiceException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.org.wang.exam.common.exception.ServiceRuntimeException;
import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.converter.ExamConverter;
import cn.org.wang.exam.converter.ExamQuAnswerConverter;
import cn.org.wang.exam.mapper.*;
import cn.org.wang.exam.model.entity.*;
import cn.org.wang.exam.model.form.exam.ExamAddForm;
import cn.org.wang.exam.model.form.exam.ExamUpdateForm;
import cn.org.wang.exam.model.form.exam_qu_answer.ExamQuAnswerAddForm;
import cn.org.wang.exam.model.vo.exam.*;
import cn.org.wang.exam.model.vo.record.ExamRecordDetailVO;
import cn.org.wang.exam.model.enums.ExamSubmitSource;
import cn.org.wang.exam.service.IExamService;
import cn.org.wang.exam.service.IOptionService;
import cn.org.wang.exam.service.IQuestionService;
import cn.org.wang.exam.service.exam.ExamSubmissionService;
import cn.org.wang.exam.utils.SecurityUtil;
import cn.org.wang.exam.websocket.WebsocketHandler;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 考试服务实现类
 *
 */
@Service
public class ExamServiceImpl extends ServiceImpl<ExamMapper, Exam> implements IExamService {

    private static final Logger logger = LoggerFactory.getLogger(ExamServiceImpl.class);
    private static final String NO_ONGOING_EXAM = "没有考试在进行";

    @Resource
    private ExamMapper examMapper;
    @Resource
    private ExamConverter examConverter;
    @Resource
    private QuestionMapper questionMapper;
    @Resource
    private IQuestionService questionService;
    @Resource
    private ExamSubjectMapper examSubjectMapper;
    @Resource
    private ExamRepoMapper examRepoMapper;
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
    private UserMapper userMapper;
    @Resource
    private ExamSubmissionService examSubmissionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> createExam(ExamAddForm examAddForm) {
        // 将关于考试相关的实体转换为Exam
        Exam exam = examConverter.formToEntity(examAddForm);
        // 计算总分
        int grossScore = calculateGrossScore(examAddForm);
        exam.setGrossScore(grossScore);
        
        // 插入考试信息
        insertExamInfo(exam);
        
        // 处理考试课程关联
        handleExamSubjects(exam, examAddForm.getSubjectIds());
        
        // 处理试卷与题库关联
        handleExamRepo(exam, examAddForm.getRepoId());
        
        // 处理题目选择
        handleQuestionSelection(exam, examAddForm);
        
        return Result.success("创建考试成功");
    }

    /**
     * 计算考试总分
     */
    private int calculateGrossScore(ExamAddForm examAddForm) {
        return examAddForm.getRadioCount() * examAddForm.getRadioScore()
                + examAddForm.getMultiCount() * examAddForm.getMultiScore()
                + examAddForm.getJudgeCount() * examAddForm.getJudgeScore()
                + examAddForm.getSaqCount() * examAddForm.getSaqScore();
    }

    /**
     * 插入考试信息
     */
    private void insertExamInfo(Exam exam) {
        int examRows = examMapper.insert(exam);
        if (examRows < 1) {
            throw new ServiceRuntimeException("添加考试到数据库失败!");
        }
    }

    /**
     * 处理考试课程关联
     */
    private void handleExamSubjects(Exam exam, String subjectIdsStr) {
        List<Integer> subjectIds = Arrays.stream(subjectIdsStr.split(","))
                .map(Integer::parseInt)
                .toList();
        Integer subjectRows = examSubjectMapper.addExamsubject(exam.getId(), subjectIds);
        if (subjectRows < 1) {
            throw new ServiceRuntimeException("创建失败!");
        }
    }

    /**
     * 处理试卷与题库关联
     */
    private void handleExamRepo(Exam exam, Integer repoId) {
        ExamRepo examRepo = new ExamRepo();
        examRepo.setExamId(exam.getId());
        examRepo.setRepoId(repoId);
        int examRepoRows = examRepoMapper.insert(examRepo);
        if (examRepoRows < 1) {
            throw new ServiceRuntimeException("创建失败!");
        }
    }

    /**
     * 处理题目选择
     */
    private void handleQuestionSelection(Exam exam, ExamAddForm examAddForm) {
        // <"试题类型"，"试题分数">
        Map<Integer, Integer> quTypeToScore = new HashMap<>();
        quTypeToScore.put(1, exam.getRadioScore());
        quTypeToScore.put(2, exam.getMultiScore());
        quTypeToScore.put(3, exam.getJudgeScore());
        quTypeToScore.put(4, exam.getSaqScore());
        // <"试题类型"，"题目数量">
        Map<Integer, Integer> quTypeToCount = new HashMap<>();
        quTypeToCount.put(1, exam.getRadioCount());
        quTypeToCount.put(2, exam.getMultiCount());
        quTypeToCount.put(3, exam.getJudgeCount());
        quTypeToCount.put(4, exam.getSaqCount());
        
        int sortCounter = 0;
        // 自己选题
        if("0".equals(examAddForm.getAddQuype())){
            handleManualQuestionSelection(exam, examAddForm, quTypeToScore, sortCounter);
        }
        // 随机抽题
        if("1".equals(examAddForm.getAddQuype())){
            handleRandomQuestionSelection(exam, examAddForm, quTypeToCount, quTypeToScore, sortCounter);
        }
    }

    /**
     * 处理自己选题
     */
    private void handleManualQuestionSelection(Exam exam, ExamAddForm examAddForm, Map<Integer, Integer> quTypeToScore, int sortCounter) {
        if(StringUtils.isBlank(examAddForm.getQuIds())){
            throw new ServiceException("自己选题的时候不能不选试题");
        }
        Integer examId = exam.getId();
        
        // 1. 获取所有选中的题目 ID 列表 (保持原始选择顺序)
        List<Integer> selectedQuIds = Arrays.stream(examAddForm.getQuIds().split(","))
                .map(Integer::parseInt)
                .toList();

        // 2. 批量查询选中的题目详情
        List<Question> selectedQuestions = questionMapper.selectBatchIds(selectedQuIds);

        // 3. 按题型分组，并保持组内相对顺序
        Map<Integer, List<Question>> groupedQuestions = groupQuestionsByType(selectedQuIds, selectedQuestions);

        // 4. 按题型顺序插入题目，并分配 sort 值
        insertQuestionsByType(examId, groupedQuestions, quTypeToScore, sortCounter);
    }

    /**
     * 按题型分组题目
     */
    private Map<Integer, List<Question>> groupQuestionsByType(List<Integer> selectedQuIds, List<Question> selectedQuestions) {
        Map<Integer, List<Question>> groupedQuestions = new LinkedHashMap<>();
        groupedQuestions.put(1, new ArrayList<>()); // 单选
        groupedQuestions.put(2, new ArrayList<>()); // 多选
        groupedQuestions.put(3, new ArrayList<>()); // 判断
        groupedQuestions.put(4, new ArrayList<>()); // 简答

        Map<Integer, Question> questionMap = selectedQuestions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        for (Integer quId : selectedQuIds) {
            Question question = questionMap.get(quId);
            if (question != null && groupedQuestions.containsKey(question.getQuType())) {
                groupedQuestions.get(question.getQuType()).add(question);
            }
        }
        return groupedQuestions;
    }

    /**
     * 按题型插入题目
     */
    private void insertQuestionsByType(Integer examId, Map<Integer, List<Question>> groupedQuestions, 
                                     Map<Integer, Integer> quTypeToScore, int sortCounter) {
        for (Map.Entry<Integer, List<Question>> entry : groupedQuestions.entrySet()) {
            Integer quType = entry.getKey();
            List<Question> questionsInGroup = entry.getValue();
            Integer quScore = quTypeToScore.get(quType);

            if (quScore == null) {
                continue; // 跳过此类型
            }

            for (Question question : questionsInGroup) {
                Map<String, Object> detail = new HashMap<>();
                detail.put("questionId", question.getId());
                detail.put("sort", sortCounter);
                sortCounter++; // 递增 sort 值

                int examQueRows = examQuestionMapper.insertSingleQuestion(examId, quType, quScore, detail);
                if (examQueRows < 1) {
                    throw new ServiceRuntimeException("创建考试失败，插入题目关联时出错, Question ID: " + question.getId());
                }
            }
        }
    }

    /**
     * 处理随机抽题
     */
    private void handleRandomQuestionSelection(Exam exam, ExamAddForm examAddForm, 
                                             Map<Integer, Integer> quTypeToCount, 
                                             Map<Integer, Integer> quTypeToScore, 
                                             int sortCounter) {
        for (Map.Entry<Integer, Integer> entry : quTypeToCount.entrySet()) {
            // 获取当前试题类型、试题数量、考试id、试题分数
            Integer quType = entry.getKey();
            Integer count = entry.getValue();
            Integer examId = exam.getId();
            Integer quScore = quTypeToScore.get(quType);
            
            // 查询设置题库中，对应类型的试题id
            List<Question> questionsByType = getQuestionsByType(quType, examAddForm.getRepoId());
            if (questionsByType.size() < count) {
                throw new ServiceRuntimeException("题库中类型为" + quType + "的题目数量不足" + count + "个！");
            }
            
            // 随机抽取题目
            List<Integer> sampledIds = sampleQuestions(questionsByType, count);
            
            // 插入试题
            if (!sampledIds.isEmpty()) {
                insertRandomQuestions(examId, quType, quScore, sampledIds, sortCounter);
                sortCounter += sampledIds.size();
            }
        }
    }

    /**
     * 根据类型查询题目
     */
    private List<Question> getQuestionsByType(Integer quType, Integer repoId) {
        LambdaQueryWrapper<Question> typeQueryWrapper = new LambdaQueryWrapper<>();
        typeQueryWrapper.select(Question::getId)
                .eq(Question::getQuType, quType)
                .eq(Question::getIsDeleted, 0)
                .eq(Question::getRepoId, repoId);
        return questionMapper.selectList(typeQueryWrapper);
    }

    /**
     * 随机抽取题目
     */
    private List<Integer> sampleQuestions(List<Question> questionsByType, int count) {
        List<Integer> typeQuestionIds = new ArrayList<>(questionsByType.stream().map(Question::getId).toList());
        Collections.shuffle(typeQuestionIds);
        return typeQuestionIds.subList(0, count);
    }

    /**
     * 插入随机抽取的题目
     */
    private void insertRandomQuestions(Integer examId, Integer quType, Integer quScore, 
                                      List<Integer> sampledIds, int sortCounter) {
        Map<Integer, Integer> questionSortMap = new HashMap<>();
        for (Integer qId : sampledIds) {
            questionSortMap.put(qId, sortCounter);
            sortCounter++;
        }
        
        // 准备数据结构
        List<Map<String, Object>> questionDetails = new ArrayList<>();
        for (Map.Entry<Integer, Integer> sortEntry : questionSortMap.entrySet()) {
            Map<String, Object> detail = new HashMap<>();
            detail.put("questionId", sortEntry.getKey());
            detail.put("sort", sortEntry.getValue());
            questionDetails.add(detail);
        }
        
        int examQueRows = examQuestionMapper.insertQuestion(examId, quType, quScore, questionDetails);
        if (examQueRows < 1) {
            throw new ServiceRuntimeException("创建考试失败");
        }
    }

    /**
     * 获取试卷总分
     *
     * @param exam 试卷对象
     * @return
     */
    public Integer getGrossScore(Exam exam) {
        Integer grossScore = 0;
        try {
            grossScore = exam.getRadioCount() * exam.getRadioScore()
                    + exam.getMultiCount() * exam.getMultiScore()
                    + exam.getJudgeCount() * exam.getJudgeScore()
                    + exam.getSaqCount() * exam.getSaqScore();
        } catch (Exception e) {
            throw new ServiceRuntimeException("计算总分时出现空指针异常:" + e.getMessage());
        }
        return grossScore;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateExam(ExamUpdateForm examUpdateForm, Integer examId) {
        // 更具ID获取试卷
        Exam examTemp = this.getById(examId);
        // 获取试卷总分
        Integer grossScore = getGrossScore(examTemp);
        // Form转换为实体类
        Exam exam = examConverter.formToEntity(examUpdateForm);
        exam.setId(examId);
        // 设置总分
        exam.setGrossScore(grossScore);
        // 更新试卷
        Integer resultRow = examMapper.updateById(exam);
        if (resultRow < 1) {
            throw new ServiceRuntimeException("修改试卷失败");
        }
        return Result.success("修改试卷成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> deleteExam(String ids) {
        // 将ID字符串转换为列表
        List<Integer> examIds = Arrays.stream(ids.split(","))
                .map(Integer::parseInt)
                .toList();
        
        // 先获取需要通知的学生ID列表
        List<Integer> studentIds = new ArrayList<>();
        for (Integer examId : examIds) {
            // 查询正在参加该考试的学生
            LambdaQueryWrapper<UserExamsScore> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserExamsScore::getExamId, examId);
            List<UserExamsScore> userExamsScores = userExamsScoreMapper.selectList(wrapper);
            for (UserExamsScore ues : userExamsScores) {
                studentIds.add(ues.getUserId());
            }
        }
        
        // 彻底删除相关数据
        for (Integer examId : examIds) {
            // 删除学生考试记录
            LambdaQueryWrapper<UserExamsScore> uesWrapper = new LambdaQueryWrapper<>();
            uesWrapper.eq(UserExamsScore::getExamId, examId);
            userExamsScoreMapper.delete(uesWrapper);
            
            // 删除学生答题记录
            LambdaQueryWrapper<ExamQuAnswer> eqWrapper = new LambdaQueryWrapper<>();
            eqWrapper.eq(ExamQuAnswer::getExamId, examId);
            examQuAnswerMapper.delete(eqWrapper);
            
            // 删除考试题目关联
            LambdaQueryWrapper<ExamQuestion> eqWrapper2 = new LambdaQueryWrapper<>();
            eqWrapper2.eq(ExamQuestion::getExamId, examId);
            examQuestionMapper.delete(eqWrapper2);
        }
        
        // 逻辑删除试卷
        int row = examMapper.deleteBatchIds(examIds);
        if (row < 1) {
            throw new ServiceRuntimeException("删除失败，删除考试表时失败");
        }
        
        // 通知相关学生考试已被删除
        if (!studentIds.isEmpty()) {
            // 构建通知消息
            Map<String, Object> message = new HashMap<>();
            message.put("type", "EXAM_DELETED");
            message.put("data", Map.of(
                "examIds", examIds,
                "message", "您正在参加的考试已被教师删除"
            ));
            // 发送WebSocket通知
            WebsocketHandler.sendToUsers(message, studentIds);
        }
        
        return Result.success("删除试卷成功");
    }

    @Override
    public Result<IPage<ExamVO>> getPagingExam(Integer pageNum, Integer pageSize, String title) {
        // 创建Page对象
        Page<Exam> page = new Page<>(pageNum, pageSize);
        // 开始查询
        LambdaQueryWrapper<Exam> examQuery = new LambdaQueryWrapper<>();
        examQuery.like(StringUtils.isNotBlank(title), Exam::getTitle, title)
                .eq(Exam::getIsDeleted, 0);
        if (SecurityUtil.getRoleCode() == 1) {
            examQuery.eq(Exam::getUserId, SecurityUtil.getUserId());
        }
        Page<Exam> examPage = examMapper.selectPage(page, examQuery);
        // 实体转换
        Page<ExamVO> examVOPage = examConverter.pageEntityToVo(examPage);
        return Result.success("查询成功", examVOPage);
    }

    @Override
    public Result<ExamQuestionListVO> getQuestionList(Integer examId) {
        try {
            Integer userId = SecurityUtil.getUserId();
            logger.info("=== 获取试题列表调试信息 ===");
            logger.info("用户ID: {}, 考试ID: {}", userId, examId);
            
            // 检查是否正在考试
            if (!checkIfUserTakingExam(examId)) {
                return Result.failed("没有正在进行的考试");
            }
            
            ExamQuestionListVO examQuestionListVO = new ExamQuestionListVO();
            
            // 获取考试基本信息
            Exam exam = getExamInfo(examId);
            if (exam == null) {
                return Result.failed("考试不存在");
            }
            
            // 设置考试时长
            examQuestionListVO.setExamDuration(exam.getExamDuration());
            
            // 获取考试开始时间并设置（转换为时间戳毫秒）
            LocalDateTime startTime = getUserStarExamTime(examId, userId);
            if (startTime != null) {
                long startTimeMillis = startTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
                examQuestionListVO.setStartTime(startTimeMillis);
                logger.info("考试开始时间戳: {}", startTimeMillis);
            }
            
            // 计算并设置剩余时间
            long leftSeconds = calculateRemainingTime(examId, userId, exam.getExamDuration());
            examQuestionListVO.setLeftSeconds(leftSeconds);
            
            // 添加试题列表
            addQuestionsToList(examQuestionListVO, examId, userId);
            
            // 记录返回数据日志
            logQuestionListData(examQuestionListVO);
            
            logger.info("=== 获取试题列表调试结束 ===");
            
            return Result.success("查询成功", examQuestionListVO);
            
        } catch (Exception e) {
            logger.error("获取试题列表异常: {}", e.getMessage(), e);
            return Result.failed("系统错误");
        }
    }

    /**
     * 检查用户是否正在考试
     */
    private boolean checkIfUserTakingExam(Integer examId) {
        boolean isTakingExam = isUserTakingExam(examId);
        logger.info("是否正在考试: {}", isTakingExam);
        
        if (!isTakingExam) {
            logger.info("没有正在进行的考试，返回错误");
        }
        return isTakingExam;
    }

    /**
     * 获取考试信息
     */
    private Exam getExamInfo(Integer examId) {
        Exam exam = this.getById(examId);
        if (exam == null) {
            logger.info("考试不存在，ID: {}", examId);
        } else {
            logger.info("考试时长: {}分钟", exam.getExamDuration());
        }
        return exam;
    }

    /**
     * 计算剩余时间
     */
    private long calculateRemainingTime(Integer examId, Integer userId, int examDuration) {
        // 获取考试开始时间
        LocalDateTime createTime = getUserStarExamTime(examId, userId);
        if (createTime == null) {
            logger.info("无法获取考试开始时间");
            throw new ServiceRuntimeException("无法获取考试开始时间");
        }
        
        logger.info("考试开始时间: {}", createTime);
        
        // 计算考试结束时间
        LocalDateTime endTime = createTime.plusMinutes(examDuration);
        LocalDateTime now = LocalDateTime.now();
        
        logger.info("当前服务器时间: {}", now);
        logger.info("考试结束时间: {}", endTime);
        
        // 计算剩余秒数
        long leftSeconds = Duration.between(now, endTime).getSeconds();
        logger.info("计算出的剩余秒数: {}", leftSeconds);
        
        // 转换成分秒格式便于查看
        long minutes = leftSeconds / 60;
        long seconds = leftSeconds % 60;
        logger.info("剩余时间: {}分{}秒", minutes, seconds);
        
        // 处理负值情况
        if (leftSeconds < 0) {
            leftSeconds = 0;
            logger.info("考试已超时，剩余秒数设为0");
        }
        
        logger.info("最终设置的剩余秒数: {}", leftSeconds);
        return leftSeconds;
    }

    /**
     * 添加试题到列表
     */
    private void addQuestionsToList(ExamQuestionListVO examQuestionListVO, Integer examId, Integer userId) {
        logger.info("开始获取试题列表...");
        for (Integer quType = 1; quType <= 4; quType++) {
            List<ExamQuestion> examQuestionList = examQuestionMapper.getExamQuByExamIdAndQuType(examId, quType);
            logger.info("题型 {} 的题目数量: {}", quType, examQuestionList.size());
            
            if (!examQuestionList.isEmpty()) {
                List<ExamQuestionVO> examQuestionVOS = examConverter.examQuestionListEntityToVO(examQuestionList);
                
                // 处理每个题目
                processExamQuestions(examQuestionVOS, examId, userId);
                
                // 根据不同试题类型设置
                setQuestionsByType(examQuestionListVO, quType, examQuestionVOS);
            }
        }
    }

    /**
     * 处理考试题目
     * 优化 N+1 查询：使用批量查询替代循环查询
     */
    private void processExamQuestions(List<ExamQuestionVO> examQuestionVOS, Integer examId, Integer userId) {
        if (examQuestionVOS.isEmpty()) {
            return;
        }

        // 批量获取题目ID列表
        List<Integer> questionIds = examQuestionVOS.stream()
                .map(ExamQuestionVO::getQuestionId)
                .toList();

        // 批量查询题目信息（避免 N+1 查询）
        Map<Integer, Question> questionMap = questionService.listByIds(questionIds).stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        // 批量查询用户答题记录（避免 N+1 查询）
        LambdaQueryWrapper<ExamQuAnswer> answerWrapper = new LambdaQueryWrapper<>();
        answerWrapper.eq(ExamQuAnswer::getExamId, examId)
                .eq(ExamQuAnswer::getUserId, userId)
                .in(ExamQuAnswer::getQuestionId, questionIds);
        List<ExamQuAnswer> userAnswers = examQuAnswerMapper.selectList(answerWrapper);
        Map<Integer, List<ExamQuAnswer>> answerMap = userAnswers.stream()
                .collect(Collectors.groupingBy(ExamQuAnswer::getQuestionId));

        // 批量查询选项信息（避免 N+1 查询）
        LambdaQueryWrapper<Option> optionWrapper = new LambdaQueryWrapper<>();
        optionWrapper.in(Option::getQuId, questionIds)
                .orderByAsc(Option::getSort);
        List<Option> allOptions = optionMapper.selectList(optionWrapper);
        Map<Integer, List<Option>> optionMap = allOptions.stream()
                .collect(Collectors.groupingBy(Option::getQuId));

        // 处理每个题目
        for (ExamQuestionVO temp : examQuestionVOS) {
            // 检查是否已作答
            List<ExamQuAnswer> questionAnswers = answerMap.getOrDefault(temp.getQuestionId(), Collections.emptyList());
            temp.setCheckout(!questionAnswers.isEmpty());

            // 获取题目信息
            Question question = questionMap.get(temp.getQuestionId());
            if (question != null) {
                temp.setContent(question.getContent());
                temp.setImage(question.getImage());

                // 获取选项信息
                List<Option> options = optionMap.getOrDefault(temp.getQuestionId(), Collections.emptyList());
                temp.setOptions(examConverter.opListEntityToVO(options));
            }
        }
    }

    /**
     * 检查题目是否已作答
     */
    private void checkIfAnswered(ExamQuestionVO temp, Integer examId, Integer userId) {
        LambdaQueryWrapper<ExamQuAnswer> examQuAnswerLambdaQueryWrapper = new LambdaQueryWrapper<>();
        examQuAnswerLambdaQueryWrapper.eq(ExamQuAnswer::getQuestionId, temp.getQuestionId())
                .eq(ExamQuAnswer::getExamId, examId)
                .eq(ExamQuAnswer::getUserId, userId);
        List<ExamQuAnswer> examQuAnswers = examQuAnswerMapper.selectList(examQuAnswerLambdaQueryWrapper);
        temp.setCheckout(!examQuAnswers.isEmpty());
    }

    /**
     * 获取题目选项
     */
    private void getQuestionOptions(ExamQuestionVO temp) {
        LambdaQueryWrapper<Option> optionLambdaQueryWrapper = new LambdaQueryWrapper<>();
        optionLambdaQueryWrapper.eq(Option::getQuId, temp.getQuestionId())
                .orderByAsc(Option::getSort);
        List<Option> options = optionMapper.selectList(optionLambdaQueryWrapper);
        temp.setOptions(examConverter.opListEntityToVO(options));
    }

    /**
     * 根据题型设置题目列表
     */
    private void setQuestionsByType(ExamQuestionListVO examQuestionListVO, Integer quType, List<ExamQuestionVO> examQuestionVOS) {
        switch (quType) {
            case 1:
                examQuestionListVO.setRadioList(examQuestionVOS);
                break;
            case 2:
                examQuestionListVO.setMultiList(examQuestionVOS);
                break;
            case 3:
                examQuestionListVO.setJudgeList(examQuestionVOS);
                break;
            case 4:
                examQuestionListVO.setSaqList(examQuestionVOS);
                break;
            default:
                logger.warn("未知题型: {}", quType);
                break;
        }
    }

    /**
     * 记录题目列表数据日志
     */
    private void logQuestionListData(ExamQuestionListVO examQuestionListVO) {
        logger.info("返回的VO数据:");
        logger.info("- examDuration: {}", examQuestionListVO.getExamDuration());
        logger.info("- leftSeconds: {}", examQuestionListVO.getLeftSeconds());
        logger.info("- radioList size: {}", (examQuestionListVO.getRadioList() != null ? examQuestionListVO.getRadioList().size() : 0));
        logger.info("- multiList size: {}", (examQuestionListVO.getMultiList() != null ? examQuestionListVO.getMultiList().size() : 0));
        logger.info("- judgeList size: {}", (examQuestionListVO.getJudgeList() != null ? examQuestionListVO.getJudgeList().size() : 0));
        logger.info("- saqList size: {}", (examQuestionListVO.getSaqList() != null ? examQuestionListVO.getSaqList().size() : 0));
    }

    @Override
    public Result<ExamQuDetailVO> getQuestionSingle(Integer examId, Integer quId) {
        // 检查是否正在考试
        if (!isUserTakingExam(examId)) {
            return Result.failed(NO_ONGOING_EXAM);
        }
        
        ExamQuDetailVO examQuDetailVO = new ExamQuDetailVO();
        
        // 查询 ExamQuestion 信息
        ExamQuestion examQuestion = getExamQuestion(examId, quId);
        examQuDetailVO.setSort(examQuestion.getSort());
        
        // 查询 Question 信息并设置基本属性
        Question quById = questionService.getById(quId);
        examQuDetailVO.setImage(quById.getImage());
        examQuDetailVO.setContent(quById.getContent());
        examQuDetailVO.setQuType(quById.getQuType());
        
        // 处理选项和用户作答情况
        List<OptionVO> optionVOS = processOptionsAndUserAnswers(examId, quId, examQuDetailVO);
        
        // 根据题目类型设置答案列表
        if (quById.getQuType() != 4) {
            examQuDetailVO.setAnswerList(optionVOS);
        }
        
        return Result.success("获取成功", examQuDetailVO);
    }
    
    /**
     * 查询 ExamQuestion 信息
     */
    private ExamQuestion getExamQuestion(Integer examId, Integer quId) {
        LambdaQueryWrapper<ExamQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamQuestion::getQuestionId, quId)
               .eq(ExamQuestion::getExamId, examId);
        return examQuestionMapper.selectOne(wrapper);
    }
    
    /**
     * 处理选项和用户作答情况
     */
    private List<OptionVO> processOptionsAndUserAnswers(Integer examId, Integer quId, ExamQuDetailVO examQuDetailVO) {
        // 查询选项列表
        LambdaQueryWrapper<Option> optionWrapper = new LambdaQueryWrapper<>();
        optionWrapper.eq(Option::getQuId, quId);
        List<Option> options = optionMapper.selectList(optionWrapper);
        List<OptionVO> optionVOS = examConverter.opListEntityToVO(options);
        
        // 处理每个选项的用户作答情况
        for (OptionVO optionVO : optionVOS) {
            processUserAnswerForOption(examId, optionVO, examQuDetailVO);
        }
        
        return optionVOS;
    }
    
    /**
     * 处理单个选项的用户作答情况
     */
    private void processUserAnswerForOption(Integer examId, OptionVO optionVO, ExamQuDetailVO examQuDetailVO) {
        // 查询用户作答记录
        LambdaQueryWrapper<ExamQuAnswer> answerWrapper = new LambdaQueryWrapper<>();
        answerWrapper.eq(ExamQuAnswer::getQuestionId, optionVO.getQuId())
                    .eq(ExamQuAnswer::getExamId, examId)
                    .eq(ExamQuAnswer::getUserId, SecurityUtil.getUserId());
        List<ExamQuAnswer> examQuAnswers = examQuAnswerMapper.selectList(answerWrapper);
        
        if (!examQuAnswers.isEmpty()) {
            for (ExamQuAnswer answer : examQuAnswers) {
                processAnswerForOption(answer, optionVO, examQuDetailVO);
            }
        }
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
                // 单选题和判断题
                optionVO.setCheckout(answerId.equals(optionIdStr));
                break;
            case 2:
                // 多选题
                List<Integer> selectedOptionIds = Arrays.stream(answerId.split(","))
                        .map(Integer::parseInt)
                        .toList();
                optionVO.setCheckout(selectedOptionIds.contains(optionVO.getId()));
                break;
            case 4:
                // 简答题
                optionVO.setContent(answerContent);
                examQuDetailVO.setAnswerList(Collections.singletonList(optionVO));
                break;
            default:
                break;
        }
    }

    @Override
    public Result<List<ExamQuCollectVO>> getCollect(Integer examId) {
        // 检查是否正在考试
        if (!isUserTakingExam(examId)) {
            return Result.failed(NO_ONGOING_EXAM);
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
        // 查询题干列表
        List<Question> questions = questionMapper.selectBatchIds(quIds);
        for (Question temp : questions) {
            // 创建返回对象
            ExamQuCollectVO examQuCollectVO = new ExamQuCollectVO();
            // 设置标题
            examQuCollectVO.setTitle(temp.getContent());
            examQuCollectVO.setQuType(temp.getQuType());
            // 设置题目ID
            examQuCollectVO.setId(temp.getId());

            // 查询试题选项
            LambdaQueryWrapper<Option> optionWrapper = new LambdaQueryWrapper<>();
            optionWrapper.eq(Option::getQuId, temp.getId());
            List<Option> options = optionMapper.selectList(optionWrapper);
            if (temp.getQuType() == 4) {
                examQuCollectVO.setOption(null);
            } else {
                examQuCollectVO.setOption(options);
            }


            // 设置是否正确
            LambdaQueryWrapper<ExamQuAnswer> examQuAnswerWrapper = new LambdaQueryWrapper<>();
            examQuAnswerWrapper.eq(ExamQuAnswer::getUserId, SecurityUtil.getUserId())
                    .eq(ExamQuAnswer::getExamId, examId)
                    .eq(ExamQuAnswer::getQuestionId, temp.getId());
            ExamQuAnswer examQuAnswer = examQuAnswerMapper.selectOne(examQuAnswerWrapper);
            // 如果某题没有作答
            if (examQuAnswer == null) {
                examQuCollectVO.setMyOption(null);
                examQuCollectVOS.add(examQuCollectVO);
                continue;
            }
            switch (temp.getQuType()) {
                case 1:
                    // 设置自己的选项
                    LambdaQueryWrapper<Option> optionLambdaQueryWrapper1 = new LambdaQueryWrapper<>();
                    optionLambdaQueryWrapper1.eq(Option::getId, examQuAnswer.getAnswerId());
                    Option op1 = optionMapper.selectOne(optionLambdaQueryWrapper1);
                    examQuCollectVO.setMyOption(Integer.toString(op1.getSort()));
                    break;
                case 2:
                    // 将回答id解析为列表
                    String answerId = examQuAnswer.getAnswerId();
                    List<Integer> opIds = Arrays.stream(answerId.split(","))
                            .map(Integer::parseInt)
                            .toList();
                    // 添加选项顺序
                    List<Integer> sorts = new ArrayList<>();
                    for (Integer opId : opIds) {
                        LambdaQueryWrapper<Option> optionLambdaQueryWrapper2 = new LambdaQueryWrapper<>();
                        optionLambdaQueryWrapper2.eq(Option::getId, opId);
                        Option option = optionMapper.selectOne(optionLambdaQueryWrapper2);
                        sorts.add(option.getSort());
                    }
                    // 设置自己选的选项，选项为顺序 1为A，2为B...
                    List<String> shortList = sorts.stream().map(String::valueOf).toList();
                    String myOption = String.join(",", shortList);
                    examQuCollectVO.setMyOption(myOption);
                    break;
                case 3:
                    // 查询自己的的选项
                    LambdaQueryWrapper<Option> optionLambdaQueryWrapper3 = new LambdaQueryWrapper<>();
                    optionLambdaQueryWrapper3.eq(Option::getId, examQuAnswer.getAnswerId());
                    Option op3 = optionMapper.selectOne(optionLambdaQueryWrapper3);
                    examQuCollectVO.setMyOption(Integer.toString(op3.getSort()));
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
    public Result<ExamDetailVO> getDetail(Integer examId) {
        // 查询考试详情信息
        Exam exam = this.getById(examId);
        // 实体转换
        ExamDetailVO examDetailVO = examConverter.examToExamDetailVO(exam);
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getId, examDetailVO.getUserId());
        User user = userMapper.selectOne(userLambdaQueryWrapper);
        examDetailVO.setUsername(user.getRealName());
        return Result.success("查询成功", examDetailVO);
    }

    @Override
    public Result<Integer> addCheat(Integer examId) {
        // 查询条件包含state=0，确保只处理正在进行的考试
        LambdaQueryWrapper<UserExamsScore> userExamsScoreLambdaQuery = new LambdaQueryWrapper<>();
        userExamsScoreLambdaQuery.eq(UserExamsScore::getExamId, examId)
                .eq(UserExamsScore::getUserId, SecurityUtil.getUserId())
                .eq(UserExamsScore::getState, 0); // 只查询正在进行的考试
        UserExamsScore userExamsScore = userExamsScoreMapper.selectOne(userExamsScoreLambdaQuery);
        
        // 检查考试记录是否存在
        if (userExamsScore == null) {
            return Result.failed("没有正在进行的考试");
        }
        
        Exam exam = this.getById(examId);
        if (exam == null) {
            return Result.failed("考试不存在");
        }
        
        // 计算新的切屏次数
        int currentCount = userExamsScore.getCount() != null ? userExamsScore.getCount() : 0;
        int newCount = currentCount + 1;
        
        // 操作次数，自动交卷
        if (exam.getMaxCount() != null && newCount >= exam.getMaxCount()) {
            // 达到最大切屏次数，触发强制交卷
            logger.info("切屏次数达到上限，触发自动交卷，用户ID: {}, 考试ID: {}, 切屏次数: {}", 
                    SecurityUtil.getUserId(), examId, newCount);
            
            // 先更新切屏次数
            LambdaUpdateWrapper<UserExamsScore> updateCountWrapper = new LambdaUpdateWrapper<>();
            updateCountWrapper.eq(UserExamsScore::getExamId, examId)
                    .eq(UserExamsScore::getUserId, SecurityUtil.getUserId())
                    .eq(UserExamsScore::getState, 0)
                    .set(UserExamsScore::getCount, newCount);
            userExamsScoreMapper.update(null, updateCountWrapper);
            
            // 触发交卷
            Result<ExamQuDetailVO> handExamResult = examSubmissionService.submitExam(examId, ExamSubmitSource.FORCE);
            if (handExamResult.getCode() == 1) {
                return Result.success("切屏次数超过限制，已自动交卷", 1);
            } else {
                return Result.failed("自动交卷失败: " + handExamResult.getMsg());
            }
        }
        // 更新切屏次数
        LambdaUpdateWrapper<UserExamsScore> userExamsScoreLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        userExamsScoreLambdaUpdateWrapper.eq(UserExamsScore::getExamId, examId)
                .eq(UserExamsScore::getUserId, SecurityUtil.getUserId())
                .eq(UserExamsScore::getState, 0) // 确保只更新正在进行的考试
                .set(UserExamsScore::getCount, newCount);
        userExamsScoreMapper.update(null, userExamsScoreLambdaUpdateWrapper);
        
        return Result.success("提示：请勿切屏，已切屏次数:" + newCount + "，超过限制次数将强制交卷！", 0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> addAnswer(ExamQuAnswerAddForm examQuAnswerForm) {
        if (!isUserTakingExam(examQuAnswerForm.getExamId())) {
            return Result.failed(NO_ONGOING_EXAM);
        }
        // 查询试题类型
        LambdaQueryWrapper<Question> quWrapper = new LambdaQueryWrapper<>();
        quWrapper.eq(Question::getId, examQuAnswerForm.getQuId());
        Question qu = questionMapper.selectOne(quWrapper);
        Integer quType = qu.getQuType();
        // 查询是否有记录
        LambdaQueryWrapper<ExamQuAnswer> examQuAnswerLambdaQueryWrapper = new LambdaQueryWrapper<>();
        examQuAnswerLambdaQueryWrapper.eq(ExamQuAnswer::getExamId, examQuAnswerForm.getExamId())
                .eq(ExamQuAnswer::getQuestionId, examQuAnswerForm.getQuId())
                .eq(ExamQuAnswer::getUserId, SecurityUtil.getUserId());
        List<ExamQuAnswer> existingAnswers = examQuAnswerMapper.selectList(examQuAnswerLambdaQueryWrapper);
        if (!existingAnswers.isEmpty()) {
            // 更新逻辑，这里根据题型合并处理逻辑
            return updateAnswerIfExists(examQuAnswerForm, quType);
        } else {
            // 插入逻辑，同样根据题型处理
            return insertNewAnswer(examQuAnswerForm, quType);
        }
    }

    @Override
    public Result<String> insertNewAnswer(ExamQuAnswerAddForm examQuAnswerForm, Integer quType) {
        // 检查答案是否为空
        if (examQuAnswerForm.getAnswer() == null || examQuAnswerForm.getAnswer().trim().isEmpty()) {
            // 答案为空，直接返回成功（表示清空答案）
            return Result.success("请求成功");
        }
        
        // 根据试题类型进行处理
        ExamQuAnswer examQuAnswer = prepareExamQuAnswer(examQuAnswerForm, quType);
        
        switch (quType) {
            case 1:
                return insertSingleChoiceAnswer(examQuAnswerForm, examQuAnswer);
            case 2:
                return insertMultipleChoiceAnswer(examQuAnswerForm, examQuAnswer);
            case 3:
                return insertJudgeAnswer(examQuAnswerForm, examQuAnswer);
            case 4:
                return insertShortAnswer(examQuAnswer);
            default:
                return Result.failed("请求错误，请联系管理员解决");
        }
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
        // 查找正确答案
        List<Option> correctOptions = getCorrectOptions(form.getQuId());
        // 解析用户作答
        List<Integer> userOptionIds = parseAnswerIds(form.getAnswer());
        // 判断是否正确
        boolean isRight = isMultipleChoiceCorrect(correctOptions, userOptionIds);
        answer.setIsRight(isRight ? 1 : 0);
        examQuAnswerMapper.insert(answer);
        return Result.success("请求成功");
    }
    
    /**
     * 插入判断题答案
     */
    private Result<String> insertJudgeAnswer(ExamQuAnswerAddForm form, ExamQuAnswer answer) {
        return insertSingleChoiceAnswer(form, answer);
    }
    
    /**
     * 插入简答题答案
     */
    private Result<String> insertShortAnswer(ExamQuAnswer answer) {
        // 简答题设置为未批改状态，由教师手动批改
        answer.setIsRight(-1);
        examQuAnswerMapper.insert(answer);
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

    @Override
    public Result<String> updateAnswerIfExists(ExamQuAnswerAddForm examQuAnswerForm, Integer quType) {
        // 检查答案是否为空
        if (examQuAnswerForm.getAnswer() == null || examQuAnswerForm.getAnswer().trim().isEmpty()) {
            // 答案为空，删除该题目的答案记录
            clearAnswerRecord(examQuAnswerForm);
            return Result.success("请求成功");
        }
        
        // 根据试题类型进行修改
        switch (quType) {
            case 1:
                return updateSingleChoiceAnswer(examQuAnswerForm);
            case 2:
                return updateMultipleChoiceAnswer(examQuAnswerForm);
            case 3:
                return updateJudgeAnswer(examQuAnswerForm);
            case 4:
                return updateShortAnswer(examQuAnswerForm);
            default:
                return Result.failed("请求错误，请联系管理员解决");
        }
    }
    
    /**
     * 清空答案记录
     */
    private void clearAnswerRecord(ExamQuAnswerAddForm form) {
        LambdaUpdateWrapper<ExamQuAnswer> deleteWrapper = new LambdaUpdateWrapper<>();
        deleteWrapper.eq(ExamQuAnswer::getUserId, SecurityUtil.getUserId())
                .eq(ExamQuAnswer::getExamId, form.getExamId())
                .eq(ExamQuAnswer::getQuestionId, form.getQuId())
                .set(ExamQuAnswer::getCheckout, false);
        examQuAnswerMapper.update(null, deleteWrapper);
    }
    
    /**
     * 更新单选题答案
     */
    private Result<String> updateSingleChoiceAnswer(ExamQuAnswerAddForm form) {
        Option option = optionService.getById(form.getAnswer());
        if (option == null) {
            return Result.failed("数据库中不存在该试题，请联系管理员解决");
        }
        
        LambdaUpdateWrapper<ExamQuAnswer> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ExamQuAnswer::getUserId, SecurityUtil.getUserId())
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
    private Result<String> updateMultipleChoiceAnswer(ExamQuAnswerAddForm form) {
        // 查找正确答案
        List<Option> correctOptions = getCorrectOptions(form.getQuId());
        if (correctOptions.isEmpty()) {
            return Result.failed("该题正确答案选项不存在");
        }
        
        // 解析用户作答
        List<Integer> userOptionIds = parseAnswerIds(form.getAnswer());
        // 判断答案是否正确
        boolean isRight = isMultipleChoiceCorrect(correctOptions, userOptionIds);
        
        LambdaUpdateWrapper<ExamQuAnswer> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ExamQuAnswer::getUserId, SecurityUtil.getUserId())
                .eq(ExamQuAnswer::getExamId, form.getExamId())
                .eq(ExamQuAnswer::getQuestionId, form.getQuId())
                .set(ExamQuAnswer::getAnswerId, form.getAnswer())
                .set(ExamQuAnswer::getIsRight, isRight ? 1 : 0)
                .set(ExamQuAnswer::getCheckout, true);
        
        examQuAnswerMapper.update(null, updateWrapper);
        return Result.success("请求成功");
    }
    
    /**
     * 更新判断题答案
     */
    private Result<String> updateJudgeAnswer(ExamQuAnswerAddForm form) {
        return updateSingleChoiceAnswer(form);
    }
    
    /**
     * 更新简答题答案
     */
    private Result<String> updateShortAnswer(ExamQuAnswerAddForm form) {
        LambdaUpdateWrapper<ExamQuAnswer> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ExamQuAnswer::getUserId, SecurityUtil.getUserId())
                .eq(ExamQuAnswer::getExamId, form.getExamId())
                .eq(ExamQuAnswer::getQuestionId, form.getQuId())
                .set(ExamQuAnswer::getAnswerContent, form.getAnswer())
                .set(ExamQuAnswer::getCheckout, true);
        
        examQuAnswerMapper.update(null, updateWrapper);
        return Result.success("请求成功");
    }

    @Override
    public ExamQuAnswer prepareExamQuAnswer(ExamQuAnswerAddForm form, Integer quType) {
        // 表单转换实体
        ExamQuAnswer examQuAnswer = examQuAnswerConverter.formToEntity(form);
        if (quType == 4) {
            examQuAnswer.setAnswerContent(form.getAnswer());
        } else {
            examQuAnswer.setAnswerId(form.getAnswer());
        }
        examQuAnswer.setUserId(SecurityUtil.getUserId());
        examQuAnswer.setQuestionType(quType);
        return examQuAnswer;
    }

    @Override
    public boolean isUserTakingExam(Integer examId) {
        // 判断是否正在考试
        LambdaQueryWrapper<UserExamsScore> userExamsScoreLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userExamsScoreLambdaQueryWrapper.eq(UserExamsScore::getUserId, SecurityUtil.getUserId())
                .eq(UserExamsScore::getExamId, examId)
                .eq(UserExamsScore::getState, 0);
        List<UserExamsScore> userExamsScores = userExamsScoreMapper.selectList(userExamsScoreLambdaQueryWrapper);
        return !userExamsScores.isEmpty();
    }
    
    public String getOngoingExamName() {
        // 获取用户正在进行的考试名称
        LambdaQueryWrapper<UserExamsScore> userExamsScoreLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userExamsScoreLambdaQueryWrapper.eq(UserExamsScore::getUserId, SecurityUtil.getUserId())
                .eq(UserExamsScore::getState, 0);
        UserExamsScore userExamsScore = userExamsScoreMapper.selectOne(userExamsScoreLambdaQueryWrapper);
        if (userExamsScore == null) {
            return null;
        }
        Exam exam = this.getById(userExamsScore.getExamId());
        return exam != null ? exam.getTitle() : null;
    }
    
    public boolean isUserTakingAnyExam() {
        // 判断用户是否正在参加任何考试
        LambdaQueryWrapper<UserExamsScore> userExamsScoreLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userExamsScoreLambdaQueryWrapper.eq(UserExamsScore::getUserId, SecurityUtil.getUserId())
                .eq(UserExamsScore::getState, 0);
        UserExamsScore userExamsScore = userExamsScoreMapper.selectOne(userExamsScoreLambdaQueryWrapper);
        return userExamsScore != null;
    }

    @Override
    public Result<List<ExamRecordDetailVO>> details(Integer examId) {
        // 1、题干 2、选项 3、自己的答案 4、正确的答案 5、是否正确 6、试题分析
        List<ExamRecordDetailVO> examRecordDetailVOS = new ArrayList<>();
        // 查询该考试的试题
        LambdaQueryWrapper<ExamQuestion> examQuestionWrapper = new LambdaQueryWrapper<>();
        examQuestionWrapper.eq(ExamQuestion::getExamId, examId)
                .orderByAsc(ExamQuestion::getSort);
        List<ExamQuestion> examQuestions = examQuestionMapper.selectList(examQuestionWrapper);
        
        // 创建试题ID到分数的映射
        Map<Integer, Integer> questionScoreMap = examQuestions.stream()
                .collect(Collectors.toMap(ExamQuestion::getQuestionId, ExamQuestion::getScore));
                
        List<Integer> quIds = examQuestions.stream()
                .map(ExamQuestion::getQuestionId)
                .toList();
        // 查询题干列表
        List<Question> questions = questionMapper.selectBatchIds(quIds);
        for (Question temp : questions) {
            // 创建返回对象
            ExamRecordDetailVO examRecordDetailVO = new ExamRecordDetailVO();
            // 设置标题
            examRecordDetailVO.setImage(temp.getImage());
            examRecordDetailVO.setTitle(temp.getContent());
            examRecordDetailVO.setQuType(temp.getQuType());
            // 设置分析
            examRecordDetailVO.setAnalyse(temp.getAnalysis());
            // 设置题目分数
            examRecordDetailVO.setScore(questionScoreMap.getOrDefault(temp.getId(), 1));
            // 查询试题选项
            LambdaQueryWrapper<Option> optionWrapper = new LambdaQueryWrapper<>();
            optionWrapper.eq(Option::getQuId, temp.getId());
            List<Option> options = optionMapper.selectList(optionWrapper);
            if (temp.getQuType() == 4) {
                examRecordDetailVO.setOption(null);
            } else {
                examRecordDetailVO.setOption(options);
            }

            // 设置正确答案
            LambdaQueryWrapper<Option> opWrapper = new LambdaQueryWrapper<>();
            opWrapper.eq(Option::getQuId, temp.getId());
            List<Option> opList = optionMapper.selectList(opWrapper);

            if (temp.getQuType() == 4 && !opList.isEmpty()) {
                examRecordDetailVO.setRightOption(opList.get(0).getContent());
            } else {
                ArrayList<Integer> strings = new ArrayList<>();
                for (Option temp1 : options) {
                    if (temp1.getIsRight() == 1) {
                        strings.add(temp1.getSort());
                    }
                }
                List<String> stringList = strings.stream().map(String::valueOf).toList();
                String result = String.join(",", stringList);

                examRecordDetailVO.setRightOption(result);
            }
            examRecordDetailVOS.add(examRecordDetailVO);
        }

        return Result.success("查询考试的信息成功", examRecordDetailVOS);

    }

    @Override
    public Result<IPage<ExamsubjectListVO>> getsubjectExamList(Integer pageNum, Integer pageSize, String title, Boolean isASC) {
        // 创建分页对象
        IPage<ExamsubjectListVO> examPage = new Page<>(pageNum, pageSize);
        // 获取用户ID
        Integer userId = SecurityUtil.getUserId();
        // 获取用户角色
        String role = SecurityUtil.getRole();
        // 根据课程查找考试ID
        if ("role_student".equals(role)) {
            examSubjectMapper.selectClassExam(examPage, userId, title, isASC);
        } else if ("role_admin".equals(role)) {
            examSubjectMapper.selectAdminClassExam(examPage, userId, title, isASC);
        }
        // 根据考试id查找考试
        return Result.success("查询成功", examPage);
    }


    @Override
    public Result<ExamQuDetailVO> handExam(Integer examId) {
        return examSubmissionService.submitExam(examId, ExamSubmitSource.USER);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> startExam(Integer examId) {
        Integer userId = SecurityUtil.getUserId();

        // 本场考试已有进行中记录：允许恢复进入（换设备/关页面后继续）
        LambdaQueryWrapper<UserExamsScore> ongoingThisExam = new LambdaQueryWrapper<>();
        ongoingThisExam.eq(UserExamsScore::getUserId, userId)
                .eq(UserExamsScore::getExamId, examId)
                .eq(UserExamsScore::getState, 0);
        if (userExamsScoreMapper.selectCount(ongoingThisExam) > 0) {
            return Result.success("继续考试");
        }

        // 正在参加其他考试
        LambdaQueryWrapper<UserExamsScore> otherOngoing = new LambdaQueryWrapper<>();
        otherOngoing.eq(UserExamsScore::getUserId, userId)
                .eq(UserExamsScore::getState, 0)
                .ne(UserExamsScore::getExamId, examId);
        UserExamsScore other = userExamsScoreMapper.selectOne(otherOngoing);
        if (other != null) {
            Exam otherExam = this.getById(other.getExamId());
            String title = otherExam != null ? otherExam.getTitle() : "其他考试";
            return Result.failed(title + "正在考试中");
        }

        // 已提交过本场考试
        LambdaQueryWrapper<UserExamsScore> submitted = new LambdaQueryWrapper<>();
        submitted.eq(UserExamsScore::getUserId, userId)
                .eq(UserExamsScore::getExamId, examId)
                .eq(UserExamsScore::getState, 1);
        if (userExamsScoreMapper.selectCount(submitted) > 0) {
            return Result.failed("这场考试已考不能第二次考试");
        }

        Exam exam = this.getById(examId);
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

    private LocalDateTime getUserStarExamTime(Integer examId, Integer userId) {
        LambdaQueryWrapper<UserExamsScore> userExamsScoreLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userExamsScoreLambdaQueryWrapper.eq(UserExamsScore::getUserId, userId)
                .eq(UserExamsScore::getExamId, examId)
                .eq(UserExamsScore::getState, 0);
        UserExamsScore userExamsScore = userExamsScoreMapper.selectOne(userExamsScoreLambdaQueryWrapper);
        if (userExamsScore == null) {
            return null;
        }
        return userExamsScore.getCreateTime();
    }
}

