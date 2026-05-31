package cn.org.wang.exam.service.exam.impl;

import com.aliyun.oss.ServiceException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

import cn.org.wang.exam.common.exception.ServiceRuntimeException;
import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.converter.ExamConverter;
import cn.org.wang.exam.mapper.*;
import cn.org.wang.exam.model.entity.*;
import cn.org.wang.exam.model.form.exam.ExamAddForm;
import cn.org.wang.exam.model.form.exam.ExamUpdateForm;
import cn.org.wang.exam.service.exam.ExamCreateService;
import cn.org.wang.exam.websocket.WebsocketHandler;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 考试创建服务实现类
 * 处理考试的创建、编辑、删除操作
 *
 * @author Wang
 * @version 1.0
 */
/**
 * @deprecated 生产入口为 {@link cn.org.wang.exam.service.impl.ExamServiceImpl}，待 Facade 接线后移除重复实现
 */
@Deprecated
@Service
public class ExamCreateServiceImpl implements ExamCreateService {

    private static final Logger logger = LoggerFactory.getLogger(ExamCreateServiceImpl.class);

    @Resource
    private ExamMapper examMapper;
    @Resource
    private ExamConverter examConverter;
    @Resource
    private QuestionMapper questionMapper;
    @Resource
    private ExamSubjectMapper examSubjectMapper;
    @Resource
    private ExamRepoMapper examRepoMapper;
    @Resource
    private ExamQuestionMapper examQuestionMapper;
    @Resource
    private UserExamsScoreMapper userExamsScoreMapper;
    @Resource
    private ExamQuAnswerMapper examQuAnswerMapper;

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
        if ("0".equals(examAddForm.getAddQuype())) {
            handleManualQuestionSelection(exam, examAddForm, quTypeToScore, sortCounter);
        }
        // 随机抽题
        if ("1".equals(examAddForm.getAddQuype())) {
            handleRandomQuestionSelection(exam, examAddForm, quTypeToCount, quTypeToScore, sortCounter);
        }
    }

    /**
     * 处理自己选题
     */
    private void handleManualQuestionSelection(Exam exam, ExamAddForm examAddForm, Map<Integer, Integer> quTypeToScore, int sortCounter) {
        if (StringUtils.isBlank(examAddForm.getQuIds())) {
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
                .collect(java.util.stream.Collectors.toMap(Question::getId, q -> q));

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateExam(ExamUpdateForm examUpdateForm, Integer examId) {
        // 根据ID获取试卷
        Exam examTemp = examMapper.selectById(examId);
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

    /**
     * 获取试卷总分
     */
    private Integer getGrossScore(Exam exam) {
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
}
