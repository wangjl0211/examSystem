package cn.org.wang.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.org.wang.exam.common.exception.ServiceRuntimeException;
import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.converter.ExerciseConverter;
import cn.org.wang.exam.converter.RecordConverter;
import cn.org.wang.exam.mapper.*;
import cn.org.wang.exam.model.entity.*;
import cn.org.wang.exam.model.form.exercise.ExerciseFillAnswerFrom;
import cn.org.wang.exam.model.vo.exercise.AnswerInfoVO;
import cn.org.wang.exam.model.vo.exercise.QuestionSheetVO;
import cn.org.wang.exam.model.vo.question.QuestionVO;
import cn.org.wang.exam.model.vo.record.ExamRecordDetailVO;
import cn.org.wang.exam.model.vo.record.ExamRecordDetailWithTimeVO;
import cn.org.wang.exam.service.IExerciseRecordService;
import cn.org.wang.exam.service.IOptionService;
import cn.org.wang.exam.utils.SecurityUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 考试记录服务实现类
 *
 * @author Wang
 * @since 2026-03-21
 */
@Service
public class ExerciseRecordServiceImpl extends ServiceImpl<ExerciseRecordMapper, ExerciseRecord>
        implements IExerciseRecordService {

    @Resource
    private QuestionMapper questionMapper;
    @Resource
    private ExamMapper examMapper;
    @Resource
    private RecordConverter recordConverter;
    @Resource
    private ExamQuestionMapper examQuestionMapper;
    @Resource
    private OptionMapper optionMapper;
    @Resource
    private ExamQuAnswerMapper examQuAnswerMapper;
    @Resource
    private IOptionService optionService;
    @Resource
    private UserExerciseRecordMapper userExerciseRecordMapper;
    @Resource
    private RepoMapper repoMapper;
    @Resource
    private ExerciseConverter exerciseConverter;
    @Resource
    private ExerciseRecordMapper exerciseRecordMapper;
    @Resource
    private SubjectExerciseMapper subjectExerciseMapper;
    @Resource
    private UserSubjectMapper userSubjectMapper;
    @Resource
    private ManualScoreMapper manualScoreMapper;
    @Resource
    private UserExamsScoreMapper userExamsScoreMapper;

    // 错误提示常量
    private static final String REPO_NO_COURSE_ACCESS_MSG = "该题库未关联任何课程，无法访问";
    private static final String USER_NO_COURSE_ACCESS_MSG = "您未加入该题库关联的任何课程，无法访问";
    private static final String REPO_NO_COURSE_SUBMIT_MSG = "该题库未关联任何课程，无法提交答案";
    private static final String USER_NO_COURSE_SUBMIT_MSG = "您未加入该题库关联的任何课程，无法提交答案";
    private static final String REPO_NO_COURSE_OPERATE_MSG = "该题库未关联任何课程，无法操作";
    private static final String USER_NO_COURSE_OPERATE_MSG = "您未加入该题库关联的任何课程，无法操作";

    @Override
    public Result<List<QuestionSheetVO>> getQuestionSheet(Integer repoId, Integer quType) {
        // 获取当前用户ID和角色
        Integer userId = SecurityUtil.getUserId();
        Integer roleCode = SecurityUtil.getRoleCode();
        
        // 管理员(0)和教师(1)可以直接访问
        if (roleCode == 0 || roleCode == 1) {
            List<QuestionSheetVO> list = questionMapper.selectQuestionSheet(repoId, quType, userId);
            return Result.success("获取获取试题答题卡列表成功", list);
        }
        
        // 学生用户需要验证权限
        // 获取与题库关联的课程ID
        LambdaQueryWrapper<SubjectExercise> seWrapper = new LambdaQueryWrapper<>();
        seWrapper.eq(SubjectExercise::getRepoId, repoId);
        List<SubjectExercise> subjectExercises = subjectExerciseMapper.selectList(seWrapper);
        
        if (subjectExercises.isEmpty()) {
            return Result.failed(REPO_NO_COURSE_ACCESS_MSG);
        }
        
        // 获取关联的课程ID列表
        List<Integer> subjectIds = subjectExercises.stream()
                .map(SubjectExercise::getSubjectId)
                .distinct()
                .toList();
        
        // 检查用户是否加入了任何关联的课程
        LambdaQueryWrapper<UserSubject> usWrapper = new LambdaQueryWrapper<>();
        usWrapper.eq(UserSubject::getUId, userId)
                .in(UserSubject::getGId, subjectIds)
                .eq(UserSubject::getIsDeleted, 0);
        
        List<UserSubject> userSubjects = userSubjectMapper.selectList(usWrapper);
        if (userSubjects.isEmpty()) {
            return Result.failed(USER_NO_COURSE_ACCESS_MSG);
        }
        
        // 验证通过，返回试题列表
        List<QuestionSheetVO> list = questionMapper.selectQuestionSheet(repoId, quType, userId);
        return Result.success("获取获取试题答题卡列表成功", list);
    }



    @Override
    public Result<ExamRecordDetailWithTimeVO> getExamRecordDetail(Integer examId, Integer userId) {
        if(userId==null){
            userId =SecurityUtil.getUserId();
        }
        
        // 查询该考试的试题
        List<ExamQuestion> examQuestions = getExamQuestions(examId);
        
        // 创建试题ID到分数的映射
        Map<Integer, Integer> questionScoreMap = createQuestionScoreMap(examQuestions);
        
        // 获取试题ID列表
        List<Integer> quIds = getQuestionIds(examQuestions);
        
        // 查询题干列表
        List<Question> questions = questionMapper.selectBatchIds(quIds);
        
        // 处理每个试题，生成详细信息
        List<ExamRecordDetailVO> examRecordDetailVOS = processQuestions(questions, examId, userId, questionScoreMap);

        // 构建返回对象
        ExamRecordDetailWithTimeVO result = buildResult(examRecordDetailVOS, examId, userId);

        return Result.success("查询考试的信息成功", result);
    }
    
    /**
     * 查询考试的试题
     */
    private List<ExamQuestion> getExamQuestions(Integer examId) {
        LambdaQueryWrapper<ExamQuestion> examQuestionWrapper = new LambdaQueryWrapper<>();
        examQuestionWrapper.eq(ExamQuestion::getExamId, examId)
                .orderByAsc(ExamQuestion::getSort);
        return examQuestionMapper.selectList(examQuestionWrapper);
    }
    
    /**
     * 创建试题ID到分数的映射
     */
    private Map<Integer, Integer> createQuestionScoreMap(List<ExamQuestion> examQuestions) {
        return examQuestions.stream()
                .collect(Collectors.toMap(ExamQuestion::getQuestionId, ExamQuestion::getScore));
    }
    
    /**
     * 获取试题ID列表
     */
    private List<Integer> getQuestionIds(List<ExamQuestion> examQuestions) {
        return examQuestions.stream()
                .map(ExamQuestion::getQuestionId)
                .toList();
    }
    
    /**
     * 处理每个试题，生成详细信息
     */
    private List<ExamRecordDetailVO> processQuestions(List<Question> questions, Integer examId, Integer userId, Map<Integer, Integer> questionScoreMap) {
        List<ExamRecordDetailVO> examRecordDetailVOS = new ArrayList<>();
        
        for (Question temp : questions) {
            // 创建返回对象
            ExamRecordDetailVO examRecordDetailVO = createExamRecordDetailVO(temp, questionScoreMap);
            
            // 设置选项
            setOptions(examRecordDetailVO, temp);
            
            // 设置正确答案
            setRightOption(examRecordDetailVO, temp);
            
            // 设置用户答案和正确性
            setUserAnswerAndCorrectness(examRecordDetailVO, examId, userId, temp);
            
            examRecordDetailVOS.add(examRecordDetailVO);
        }
        
        return examRecordDetailVOS;
    }
    
    /**
     * 创建ExamRecordDetailVO对象
     */
    private ExamRecordDetailVO createExamRecordDetailVO(Question question, Map<Integer, Integer> questionScoreMap) {
        ExamRecordDetailVO examRecordDetailVO = new ExamRecordDetailVO();
        // 设置标题
        examRecordDetailVO.setImage(question.getImage());
        examRecordDetailVO.setTitle(question.getContent());
        examRecordDetailVO.setQuType(question.getQuType());
        // 设置分析
        examRecordDetailVO.setAnalyse(question.getAnalysis());
        // 设置题目分数（从映射中获取，默认1分）
        examRecordDetailVO.setScore(questionScoreMap.getOrDefault(question.getId(), 1));
        // 设置默认得分为0
        examRecordDetailVO.setUserScore(0);
        return examRecordDetailVO;
    }
    
    /**
     * 设置选项
     */
    private void setOptions(ExamRecordDetailVO examRecordDetailVO, Question question) {
        LambdaQueryWrapper<Option> optionWrapper = new LambdaQueryWrapper<>();
        optionWrapper.eq(Option::getQuId, question.getId());
        List<Option> options = optionMapper.selectList(optionWrapper);
        if (question.getQuType() == 4) {
            examRecordDetailVO.setOption(null);
        } else {
            examRecordDetailVO.setOption(options);
        }
    }
    
    /**
     * 设置正确答案
     */
    private void setRightOption(ExamRecordDetailVO examRecordDetailVO, Question question) {
        LambdaQueryWrapper<Option> opWrapper = new LambdaQueryWrapper<>();
        opWrapper.eq(Option::getQuId, question.getId());
        List<Option> opList = optionMapper.selectList(opWrapper);

        if (question.getQuType() == 4 && !opList.isEmpty()) {
            examRecordDetailVO.setRightOption(opList.get(0).getContent());
        } else {
            List<Integer> rightOptionSorts = new ArrayList<>();
            for (Option temp1 : opList) {
                if (temp1.getIsRight() == 1) {
                    rightOptionSorts.add(temp1.getSort());
                }
            }
            List<String> stringList = rightOptionSorts.stream().map(String::valueOf).toList();
            String result = String.join(",", stringList);
            examRecordDetailVO.setRightOption(result);
        }
    }
    
    /**
     * 设置用户答案和正确性
     */
    private void setUserAnswerAndCorrectness(ExamRecordDetailVO examRecordDetailVO, Integer examId, Integer userId, Question question) {
        // 设置是否正确
        LambdaQueryWrapper<ExamQuAnswer> examQuAnswerWrapper = new LambdaQueryWrapper<>();
        examQuAnswerWrapper.eq(ExamQuAnswer::getUserId, userId)
                .eq(ExamQuAnswer::getExamId, examId)
                .eq(ExamQuAnswer::getQuestionId, question.getId());
        ExamQuAnswer examQuAnswer = examQuAnswerMapper.selectOne(examQuAnswerWrapper);
        
        // 如果某题没有作答
        if (examQuAnswer == null) {
            examRecordDetailVO.setMyOption(null);
            examRecordDetailVO.setIsRight(-1);
            return;
        }
        
        // 根据题型处理
        Integer quType = question.getQuType();
        switch (quType) {
            case 1:
                handleSingleChoice(examRecordDetailVO, examQuAnswer);
                break;
            case 2:
                handleMultipleChoice(examRecordDetailVO, examQuAnswer, question);
                break;
            case 3:
                handleTrueFalse(examRecordDetailVO, examQuAnswer);
                break;
            case 4:
                handleShortAnswer(examRecordDetailVO, examQuAnswer);
                break;
            default:
                break;
        }
    }
    
    /**
     * 处理单选题
     */
    private void handleSingleChoice(ExamRecordDetailVO examRecordDetailVO, ExamQuAnswer examQuAnswer) {
        // 设置自己的选项
        LambdaQueryWrapper<Option> optionLambdaQueryWrapper1 = new LambdaQueryWrapper<>();
        optionLambdaQueryWrapper1.eq(Option::getId, examQuAnswer.getAnswerId());
        Option op1 = optionMapper.selectOne(optionLambdaQueryWrapper1);
        examRecordDetailVO.setMyOption(Integer.toString(op1.getSort()));
        // 设置是否正确
        Option byId1 = optionService.getById(examQuAnswer.getAnswerId());
        if (byId1.getIsRight() == 1) {
            examRecordDetailVO.setIsRight(1);
            examRecordDetailVO.setUserScore(examRecordDetailVO.getScore()); // 正确得满分
        } else {
            examRecordDetailVO.setIsRight(0);
            examRecordDetailVO.setUserScore(0); // 错误得0分
        }
    }
    
    /**
     * 处理多选题
     */
    private void handleMultipleChoice(ExamRecordDetailVO examRecordDetailVO, ExamQuAnswer examQuAnswer, Question question) {
        // 将回答 id 解析为列表
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
        // 设置自己选的选项，选项为顺序 1 为 A，2 为 B...
        List<String> shortList = sorts.stream().map(String::valueOf).toList();
        String myOption = String.join(",", shortList);
        examRecordDetailVO.setMyOption(myOption);
        // 查找正确答案
        LambdaQueryWrapper<Option> optionWrapper1 = new LambdaQueryWrapper<>();
        optionWrapper1.eq(Option::getIsRight, 1)
                .eq(Option::getQuId, question.getId());
        List<Option> examQuAnswers = optionMapper.selectList(optionWrapper1);
        // 判断是否正确
        examRecordDetailVO.setIsRight(1);
        for (Option temp1 : examQuAnswers) {
            boolean contains = opIds.contains(temp1.getId());
            if (!contains) {
                // 只要有一个答案不是正确的则判断为错误
                examRecordDetailVO.setIsRight(0);
                break;
            }
        }
        // 设置得分
        examRecordDetailVO.setUserScore(examRecordDetailVO.getIsRight() == 1 ? examRecordDetailVO.getScore() : 0);
    }
    
    /**
     * 处理判断题
     */
    private void handleTrueFalse(ExamRecordDetailVO examRecordDetailVO, ExamQuAnswer examQuAnswer) {
        // 查询自己的的选项
        LambdaQueryWrapper<Option> optionLambdaQueryWrapper3 = new LambdaQueryWrapper<>();
        optionLambdaQueryWrapper3.eq(Option::getId, examQuAnswer.getAnswerId());
        Option op3 = optionMapper.selectOne(optionLambdaQueryWrapper3);
        examRecordDetailVO.setMyOption(Integer.toString(op3.getSort()));
        // 查询是否正确
        Option byId3 = optionService.getById(examQuAnswer.getAnswerId());
        if (byId3.getIsRight() == 1) {
            examRecordDetailVO.setIsRight(1);
            examRecordDetailVO.setUserScore(examRecordDetailVO.getScore()); // 正确得满分
        } else {
            examRecordDetailVO.setIsRight(0);
            examRecordDetailVO.setUserScore(0); // 错误得0分
        }
    }
    
    /**
     * 处理简答题
     */
    private void handleShortAnswer(ExamRecordDetailVO examRecordDetailVO, ExamQuAnswer examQuAnswer) {
        examRecordDetailVO.setMyOption(examQuAnswer.getAnswerContent());
        // 查询简答题的手动评分
        LambdaQueryWrapper<ManualScore> manualScoreWrapper = new LambdaQueryWrapper<>();
        manualScoreWrapper.eq(ManualScore::getExamQuAnswerId, examQuAnswer.getId());
        ManualScore manualScore = manualScoreMapper.selectOne(manualScoreWrapper);
        if (manualScore != null) {
            examRecordDetailVO.setUserScore(manualScore.getScore());
            // 根据得分判断是否正确
            examRecordDetailVO.setIsRight(manualScore.getScore() >= examRecordDetailVO.getScore() ? 1 : 0);
        } else {
            examRecordDetailVO.setIsRight(-1);
            examRecordDetailVO.setUserScore(0);
        }
    }
    
    /**
     * 构建返回对象
     */
    private ExamRecordDetailWithTimeVO buildResult(List<ExamRecordDetailVO> examRecordDetailVOS, Integer examId, Integer userId) {
        // 查询用户考试记录，获取用时、得分等信息
        LambdaQueryWrapper<UserExamsScore> userScoreQuery = new LambdaQueryWrapper<>();
        userScoreQuery.eq(UserExamsScore::getUserId, userId)
                .eq(UserExamsScore::getExamId, examId);
        UserExamsScore userExamsScore = userExamsScoreMapper.selectOne(userScoreQuery);

        // 查询考试信息，获取总分
        Exam exam = examMapper.selectById(examId);

        // 构建返回对象
        ExamRecordDetailWithTimeVO result = new ExamRecordDetailWithTimeVO();
        result.setQuestions(examRecordDetailVOS);

        if (userExamsScore != null) {
            result.setUserTime(userExamsScore.getUserTime());
            result.setUserScore(userExamsScore.getUserScore());
        }

        if (exam != null) {
            result.setTotalScore(exam.getGrossScore());
        }

        if (userExamsScore != null && userExamsScore.getLimitTime() != null) {
            result.setLimitTime(userExamsScore.getLimitTime().toString());
        }

        return result;
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<QuestionVO> fillAnswer(ExerciseFillAnswerFrom exerciseFillAnswerFrom) {
        // 获取当前用户ID和角色
        Integer userId = SecurityUtil.getUserId();
        Integer roleCode = SecurityUtil.getRoleCode();
        Integer repoId = exerciseFillAnswerFrom.getRepoId();
        
        // 学生用户(角色代码2)需要验证权限
        if (roleCode == 2) {
            Result<Void> permissionResult = validateStudentPermission(repoId, userId);
            if (permissionResult.getCode() != 1) {
                return Result.failed(permissionResult.getMsg());
            }
        }
        
        ExerciseRecord exerciseRecord = exerciseConverter.fromToEntity(exerciseFillAnswerFrom);
        boolean isCorrect = validateAnswer(exerciseRecord, exerciseFillAnswerFrom.getQuType());
        exerciseRecord.setIsRight(isCorrect ? 1 : 0);
        
        // 处理答题记录（捕获重复键异常）
        try {
            handleExerciseRecord(exerciseRecord, userId);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            // 如果出现重复键异常，说明已经答过题了，直接返回成功获取已有的答题记录
            // 获取已有的答题记录
            LambdaQueryWrapper<ExerciseRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ExerciseRecord::getUserId, userId)
                    .eq(ExerciseRecord::getRepoId, repoId)
                    .eq(ExerciseRecord::getQuestionId, exerciseRecord.getQuestionId());
            ExerciseRecord existingRecord = exerciseRecordMapper.selectOne(wrapper);
            if (existingRecord != null) {
                isCorrect = existingRecord.getIsRight() == 1;
            }
        }
        
        // 获取试题信息，返回给用户
        QuestionVO questionVO = questionMapper.selectSingle(exerciseRecord.getQuestionId(), userId);
        
        // 针对不同题型做出不同响应
        return buildFillAnswerResponse(exerciseRecord, questionVO, isCorrect);
    }
    
    /**
     * 验证学生权限
     */
    private Result<Void> validateStudentPermission(Integer repoId, Integer userId) {
        // 获取与题库关联的课程ID
        LambdaQueryWrapper<SubjectExercise> seWrapper = new LambdaQueryWrapper<>();
        seWrapper.eq(SubjectExercise::getRepoId, repoId);
        List<SubjectExercise> subjectExercises = subjectExerciseMapper.selectList(seWrapper);
        
        if (subjectExercises.isEmpty()) {
            return Result.failed(REPO_NO_COURSE_SUBMIT_MSG);
        }
        
        // 获取关联的课程ID列表
        List<Integer> subjectIds = subjectExercises.stream()
                .map(SubjectExercise::getSubjectId)
                .distinct()
                .toList();
        
        // 检查用户是否加入了任何关联的课程
        LambdaQueryWrapper<UserSubject> usWrapper = new LambdaQueryWrapper<>();
        usWrapper.eq(UserSubject::getUId, userId)
                .in(UserSubject::getGId, subjectIds)
                .eq(UserSubject::getIsDeleted, 0);
        
        List<UserSubject> userSubjects = userSubjectMapper.selectList(usWrapper);
        if (userSubjects.isEmpty()) {
            return Result.failed(USER_NO_COURSE_SUBMIT_MSG);
        }
        
        return Result.success();
    }
    
    /**
     * 验证答案正确性
     */
    private boolean validateAnswer(ExerciseRecord exerciseRecord, Integer quType) {
        // 主观题默认视为正确（需要手动评分）
        if (quType == 4) {
            return true;
        }
        
        // 检查答案是否为空字符串
        if (exerciseRecord.getAnswer() == null || exerciseRecord.getAnswer().trim().isEmpty()) {
            return false;
        }
        
        // 正常解析答案
        List<Integer> options = Arrays.stream(exerciseRecord.getAnswer().split(","))
                .map(Integer::parseInt).toList();
        List<Integer> rightOptions = new ArrayList<>();
        optionMapper.selectAllByQuestionId(exerciseRecord.getQuestionId()).forEach(option -> {
            if (option.getIsRight() == 1) {
                rightOptions.add(option.getId());
            }
        });
        
        if (options.size() != rightOptions.size()) {
            return false;
        }
        
        for (Integer option : options) {
            if (!rightOptions.contains(option)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 处理答题记录
     */
    private void handleExerciseRecord(ExerciseRecord exerciseRecord, Integer userId) {
        // 对是否第一次该题判断
        LambdaQueryWrapper<ExerciseRecord> wrapper = new LambdaQueryWrapper<ExerciseRecord>()
                .eq(ExerciseRecord::getUserId, userId)
                .eq(ExerciseRecord::getRepoId, exerciseRecord.getRepoId())
                .eq(ExerciseRecord::getQuestionId, exerciseRecord.getQuestionId());
        ExerciseRecord databaseRecord = exerciseRecordMapper.selectOne(wrapper);
        
        if (databaseRecord == null) {
            // 未做过该题，新增记录
            exerciseRecordMapper.insert(exerciseRecord);
            handleUserExerciseRecord(exerciseRecord.getRepoId(), userId);
        } else {
            // 已做过，修改答案
            exerciseRecord.setId(databaseRecord.getId());
            exerciseRecordMapper.updateById(exerciseRecord);
        }
    }
    
    /**
     * 处理用户刷题记录
     */
    private void handleUserExerciseRecord(Integer repoId, Integer userId) {
        // 获取该题库填作答记录
        LambdaQueryWrapper<UserExerciseRecord> wrapper = new LambdaQueryWrapper<UserExerciseRecord>()
                .eq(UserExerciseRecord::getUserId, userId)
                .eq(UserExerciseRecord::getRepoId, repoId);
        UserExerciseRecord userExerciseRecord = userExerciseRecordMapper.selectOne(wrapper);

        if (userExerciseRecord == null) {
            // 该题库用户首次刷题，添加一条记录
            int totalCount = getQuestionCountByRepoId(repoId);
            UserExerciseRecord insertRecord = new UserExerciseRecord();
            insertRecord.setExerciseCount(1);
            insertRecord.setRepoId(repoId);
            insertRecord.setTotalCount(totalCount);
            userExerciseRecordMapper.insert(insertRecord);
        } else {
            // 该题库非首次刷题，修改刷题数
            int totalCount = getQuestionCountByRepoId(repoId);
            UserExerciseRecord updateRecord = new UserExerciseRecord();
            updateRecord.setTotalCount(totalCount);
            updateRecord.setId(userExerciseRecord.getId());
            updateRecord.setExerciseCount(userExerciseRecord.getExerciseCount() + 1);
            userExerciseRecordMapper.updateById(updateRecord);
        }
    }
    
    /**
     * 获取题库试题数量
     */
    private int getQuestionCountByRepoId(Integer repoId) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<Question>()
                .eq(Question::getRepoId, repoId);
        return questionMapper.selectCount(wrapper).intValue();
    }
    
    /**
     * 构建填答响应
     */
    private Result<QuestionVO> buildFillAnswerResponse(ExerciseRecord exerciseRecord, QuestionVO questionVO, boolean isCorrect) {
        // 主观题响应
        if (exerciseRecord.getQuestionType() == 4) {
            return Result.success(null, questionVO);
        }
        
        return isCorrect ? Result.success("回答正确", questionVO) : Result.success("回答错误", questionVO);
    }

    @Override
    public Result<QuestionVO> getSingle(Integer id) {
        // 获取当前用户ID和角色
        Integer userId = SecurityUtil.getUserId();
        Integer roleCode = SecurityUtil.getRoleCode();
        
        // 获取试题信息
        QuestionVO questionVO = questionMapper.selectDetail(id, userId);
        if (questionVO == null) {
            return Result.failed("试题不存在");
        }
        
        // 获取试题所属题库ID
        Integer repoId = questionVO.getRepoId();
        
        // 学生用户(角色代码2)需要验证权限
        if (roleCode == 2) {
            // 获取与题库关联的课程ID
            LambdaQueryWrapper<SubjectExercise> seWrapper = new LambdaQueryWrapper<>();
            seWrapper.eq(SubjectExercise::getRepoId, repoId);
            List<SubjectExercise> subjectExercises = subjectExerciseMapper.selectList(seWrapper);
            
            if (subjectExercises.isEmpty()) {
                return Result.failed(REPO_NO_COURSE_ACCESS_MSG);
            }
            
            // 获取关联的课程ID列表
            List<Integer> subjectIds = subjectExercises.stream()
                    .map(SubjectExercise::getSubjectId)
                    .distinct()
                    .toList();
            
            // 检查用户是否加入了任何关联的课程
            LambdaQueryWrapper<UserSubject> usWrapper = new LambdaQueryWrapper<>();
            usWrapper.eq(UserSubject::getUId, userId)
                    .in(UserSubject::getGId, subjectIds)
                    .eq(UserSubject::getIsDeleted, 0);
            
            List<UserSubject> userSubjects = userSubjectMapper.selectList(usWrapper);
            if (userSubjects.isEmpty()) {
                return Result.failed(USER_NO_COURSE_ACCESS_MSG);
            }
        }
        
        return Result.success("查询单题成功", questionVO);
    }

    @Override
    public Result<AnswerInfoVO> getAnswerInfo(Integer repoId, Integer quId) {
        // 获取当前用户ID和角色
        Integer userId = SecurityUtil.getUserId();
        Integer roleCode = SecurityUtil.getRoleCode();
        
        // 学生用户(角色代码2)需要验证权限
        if (roleCode == 2) {
            // 获取与题库关联的课程ID
            LambdaQueryWrapper<SubjectExercise> seWrapper = new LambdaQueryWrapper<>();
            seWrapper.eq(SubjectExercise::getRepoId, repoId);
            List<SubjectExercise> subjectExercises = subjectExerciseMapper.selectList(seWrapper);
            
            if (subjectExercises.isEmpty()) {
                return Result.failed(REPO_NO_COURSE_ACCESS_MSG);
            }
            
            // 获取关联的课程ID列表
            List<Integer> subjectIds = subjectExercises.stream()
                    .map(SubjectExercise::getSubjectId)
                    .distinct()
                    .toList();
            
            // 检查用户是否加入了任何关联的课程
            LambdaQueryWrapper<UserSubject> usWrapper = new LambdaQueryWrapper<>();
            usWrapper.eq(UserSubject::getUId, userId)
                    .in(UserSubject::getGId, subjectIds)
                    .eq(UserSubject::getIsDeleted, 0);
            
            List<UserSubject> userSubjects = userSubjectMapper.selectList(usWrapper);
            if (userSubjects.isEmpty()) {
                return Result.failed(USER_NO_COURSE_ACCESS_MSG);
            }
        }
        
        QuestionVO questionVO = questionMapper.selectSingle(quId, userId);
        AnswerInfoVO answerInfoVO = exerciseConverter.quVOToAnswerInfoVO(questionVO);
        LambdaQueryWrapper<ExerciseRecord> exerciseRecordLambdaQueryWrapper = new LambdaQueryWrapper<ExerciseRecord>()
                .eq(ExerciseRecord::getRepoId, repoId)
                .eq(ExerciseRecord::getQuestionId, quId)
                .eq(ExerciseRecord::getUserId, userId);
        ExerciseRecord exerciseRecord = exerciseRecordMapper.selectOne(exerciseRecordLambdaQueryWrapper);
        if (exerciseRecord == null) {
            return Result.failed("回答记录不存在");
        }
        answerInfoVO.setAnswerContent(exerciseRecord.getAnswer());
        return exerciseRecord.getIsRight() == 1 ?
                Result.success("回答正确", answerInfoVO) : Result.success("回答错误", answerInfoVO);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> clearRecord(Integer repoId) {
        // 获取当前用户ID和角色
        Integer userId = SecurityUtil.getUserId();
        Integer roleCode = SecurityUtil.getRoleCode();
        
        // 学生用户(角色代码2)需要验证权限
        if (roleCode == 2) {
            // 获取与题库关联的课程ID
            LambdaQueryWrapper<SubjectExercise> seWrapper = new LambdaQueryWrapper<>();
            seWrapper.eq(SubjectExercise::getRepoId, repoId);
            List<SubjectExercise> subjectExercises = subjectExerciseMapper.selectList(seWrapper);
            
            if (subjectExercises.isEmpty()) {
                return Result.failed(REPO_NO_COURSE_OPERATE_MSG);
            }
            
            // 获取关联的课程ID列表
            List<Integer> subjectIds = subjectExercises.stream()
                    .map(SubjectExercise::getSubjectId)
                    .distinct()
                    .toList();
            
            // 检查用户是否加入了任何关联的课程
            LambdaQueryWrapper<UserSubject> usWrapper = new LambdaQueryWrapper<>();
            usWrapper.eq(UserSubject::getUId, userId)
                    .in(UserSubject::getGId, subjectIds)
                    .eq(UserSubject::getIsDeleted, 0);
            
            List<UserSubject> userSubjects = userSubjectMapper.selectList(usWrapper);
            if (userSubjects.isEmpty()) {
                return Result.failed(USER_NO_COURSE_OPERATE_MSG);
            }
        }
        
        try {
            // 删除用户在该题库的所有答题记录
            LambdaQueryWrapper<ExerciseRecord> exerciseRecordWrapper = new LambdaQueryWrapper<>();
            exerciseRecordWrapper.eq(ExerciseRecord::getUserId, userId)
                    .eq(ExerciseRecord::getRepoId, repoId);
            exerciseRecordMapper.delete(exerciseRecordWrapper);
            
            // 删除用户在该题库的刷题进度记录
            LambdaQueryWrapper<UserExerciseRecord> userExerciseRecordWrapper = new LambdaQueryWrapper<>();
            userExerciseRecordWrapper.eq(UserExerciseRecord::getUserId, userId)
                    .eq(UserExerciseRecord::getRepoId, repoId);
            userExerciseRecordMapper.delete(userExerciseRecordWrapper);
            
            return Result.success("清除记录成功");
        } catch (Exception e) {
            throw new ServiceRuntimeException("清除记录失败");
        }
    }
}
