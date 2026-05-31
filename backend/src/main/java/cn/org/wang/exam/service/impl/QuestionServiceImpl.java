package cn.org.wang.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.org.wang.exam.common.exception.ServiceRuntimeException;
import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.converter.QuestionConverter;
import cn.org.wang.exam.mapper.ExerciseRecordMapper;
import cn.org.wang.exam.mapper.OptionMapper;
import cn.org.wang.exam.mapper.QuestionMapper;
import cn.org.wang.exam.mapper.RepoMapper;
import cn.org.wang.exam.model.entity.ExerciseRecord;
import cn.org.wang.exam.model.entity.Option;
import cn.org.wang.exam.model.entity.Question;
import cn.org.wang.exam.model.entity.Repo;
import cn.org.wang.exam.model.form.question.QuestionExcelFrom;
import cn.org.wang.exam.model.form.question.QuestionFrom;
import cn.org.wang.exam.model.vo.question.QuestionVO;
import cn.org.wang.exam.service.IQuestionService;
import cn.org.wang.exam.utils.SecurityUtil;
import cn.org.wang.exam.utils.excel.ExcelUtils;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 试题管理实现类
 *
 * @author Wang
 * @since 2026-03-21
 */
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements IQuestionService {

    @Resource
    private QuestionConverter questionConverter;
    @Resource
    private QuestionMapper questionMapper;
    @Resource
    private OptionMapper optionMapper;
    @Resource
    private ExerciseRecordMapper exerciseRecordMapper;
    @Resource
    private RepoMapper repoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> addSingleQuestion(QuestionFrom questionFrom) {
        // 入参校验
        List<Option> options = questionFrom.getOptions();
        if (questionFrom.getQuType() != 4 && (Objects.isNull(options) || options.size() < 2)) {
            return Result.failed("非简答题的试题选项不能少于两个");
        }
        
        // 检查题库是否属于当前用户
        Integer userId = SecurityUtil.getUserId();
        Integer repoId = questionFrom.getRepoId();
        if (repoId != null) {
            Repo repo = repoMapper.selectById(repoId);
            if (repo == null) {
                return Result.failed("题库不存在");
            }
            if (!repo.getUserId().equals(userId)) {
                return Result.failed("无权操作此题库");
            }
        }
        
        Question question = questionConverter.fromToEntity(questionFrom);
        // 开始添加题干
        questionMapper.insert(question);
        // 根据试题类型添加选项
        if (question.getQuType() == 4) {
            // 简答题添加选项
            Option option = questionFrom.getOptions().get(0);
            option.setQuId(question.getId());
            optionMapper.insert(option);
        } else {
            // 非简答题添加选项
            // 把新建试题获取的id，填入选项中
            options.forEach(option -> option.setQuId(question.getId()));
            optionMapper.insertBatch(options);
        }
        return Result.success("单题添加成功");

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> deleteBatchByIds(String ids) {
        List<Integer> qIdList = Arrays.stream(ids.split(",")).map(Integer::parseInt).toList();
        // 检查所有试题是否属于当前用户
        Integer userId = SecurityUtil.getUserId();
        for (Integer questionId : qIdList) {
            Question question = questionMapper.selectById(questionId);
            if (question == null) {
                return Result.failed("试题不存在");
            }
            if (!question.getUserId().equals(userId)) {
                return Result.failed("无权删除此试题");
            }
        }
        // 删除用户刷题记录表
        LambdaUpdateWrapper<ExerciseRecord> updateWrapper = new LambdaUpdateWrapper<ExerciseRecord>()
                .in(ExerciseRecord::getQuestionId, qIdList);
        exerciseRecordMapper.delete(updateWrapper);
        // 先删除选项
        optionMapper.deleteBatchIds(qIdList);
        // 再删除试题
        questionMapper.deleteBatchIds(qIdList);
        return Result.success("批量删除试题成功");
    }

    @Override
    public Result<IPage<QuestionVO>> pagingQuestion(Integer pageNum, Integer pageSize, String title, Integer type, Integer repoId) {
        IPage<QuestionVO> page = new Page<>(pageNum, pageSize);
        // 获取用户和角色代码
        Integer userId = SecurityUtil.getUserId();
        Integer roleCode = SecurityUtil.getRoleCode();
        // 查询分页试题
        page = questionMapper.selectQuestionPage(page, userId, roleCode, title, type, repoId);
        return Result.success("分页查询试题成功", page);
    }

    @Override
    public Result<QuestionVO> querySingle(Integer id) {
        // 检查试题是否属于当前用户
        Integer userId = SecurityUtil.getUserId();
        Question question = questionMapper.selectById(id);
        if (question == null) {
            return Result.failed("试题不存在");
        }
        if (!question.getUserId().equals(userId)) {
            return Result.failed("无权访问此试题");
        }
        QuestionVO result = questionMapper.selectSingle(id, userId);
        return Result.success("根据试题id获取单题详情成功", result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateQuestion(QuestionFrom questionFrom) {
        // 检查试题是否属于当前用户
        Integer userId = SecurityUtil.getUserId();
        Question existingQuestion = questionMapper.selectById(questionFrom.getId());
        if (existingQuestion == null) {
            return Result.failed("试题不存在");
        }
        if (!existingQuestion.getUserId().equals(userId)) {
            return Result.failed("无权修改此试题");
        }
        // 修改试题
        Question question = questionConverter.fromToEntity(questionFrom);
        questionMapper.updateById(question);
        // 修改选项
        List<Option> options = questionFrom.getOptions();
        for (Option option : options) {
            optionMapper.updateById(option);
        }
        return Result.success("修改试题成功");
    }

    @SneakyThrows(Exception.class)
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> importQuestion(Integer id, MultipartFile file) {
        if (!ExcelUtils.isExcel(Objects.requireNonNull(file.getOriginalFilename()))) {
            throw new ServiceRuntimeException("该文件不是一个合法的Excel文件");
        }
        
        // 检查题库是否属于当前用户
        Integer userId = SecurityUtil.getUserId();
        Repo repo = repoMapper.selectById(id);
        if (repo == null) {
            return Result.failed("题库不存在");
        }
        if (!repo.getUserId().equals(userId)) {
            return Result.failed("无权操作此题库");
        }
        
        try {
            List<QuestionExcelFrom> questionExcelFroms = ExcelUtils.readMultipartFile(file, QuestionExcelFrom.class);
            // 类型转换
            List<QuestionFrom> list = QuestionExcelFrom.converterQuestionFrom(questionExcelFroms);
            
            for (QuestionFrom questionFrom : list) {
                Question question = questionConverter.fromToEntity(questionFrom);
                question.setRepoId(id);
                // 添加单题获取Id
                questionMapper.insert(question);
                // 批量添加选项
                List<Option> options = questionFrom.getOptions();
                final int[] count = {0};
                options.forEach(option -> {
                    // 简答题答案默认给正确
                    if (question.getQuType() == 4) {
                        option.setIsRight(1);
                    }
                    option.setSort(++count[0]);
                    option.setQuId(question.getId());
                });
                // 避免简答题没有答案
                if (!options.isEmpty()) {
                    optionMapper.insertBatch(options);
                }
            }
            
            return Result.success("导入试题成功");
        } catch (ServiceRuntimeException e) {
            // 捕获并返回业务异常，保留详细错误信息
            return Result.failed(e.getMessage());
        } catch (Exception e) {
            // 捕获其他异常
            return Result.failed("导入试题失败：" + e.getMessage());
        }
    }

}

