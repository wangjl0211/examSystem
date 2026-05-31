package cn.org.wang.exam.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;

import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.model.form.exercise.ExerciseFillAnswerFrom;
import cn.org.wang.exam.model.vo.exercise.AnswerInfoVO;
import cn.org.wang.exam.model.vo.exercise.ExerciseRepoVO;
import cn.org.wang.exam.model.vo.exercise.QuestionSheetVO;
import cn.org.wang.exam.model.vo.question.QuestionVO;
import cn.org.wang.exam.service.IExerciseRecordService;
import cn.org.wang.exam.service.IRepoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 刷题管理
 *
 * @Author Wang
 * @Version
 * @Date 2026/3/25 11:21 AM
 */
@Tag(name = "刷题管理相关接口")
@RestController
@RequestMapping("/api/exercises")
@Validated
@PreAuthorize("isAuthenticated()")  // 修复P0安全漏洞：所有接口需要认证
public class ExerciseController {

    @Resource
    private IExerciseRecordService iExerciseRecordService;
    @Resource
    private IRepoService iRepoService;

    /**
     * 获取试题Id列表
     *
     * @param repoId 题库Id
     * @param quType 试题类型
     * @return 响应结果
     */
    @Operation(summary ="获取试题Id列表")
    @GetMapping("/{repoId}")
    public Result<List<QuestionSheetVO>> getQuestion(@PathVariable("repoId") Integer repoId,
                                                     @Min(value = 1, message = "试题类型最小值应为1")
                                                     @Max(value = 4, message = "试题类型最大值应为4")
                                                     @Nullable
                                                     @RequestParam(value = "quType", required = false) Integer quType) {
        return iExerciseRecordService.getQuestionSheet(repoId, quType);
    }


    /**
     * 填充答案，并返回试题信息
     *
     * @param exerciseFillAnswerFrom 请求参数
     * @return 响应结果
     */
    @Operation(summary ="填充答案，并返回试题信息")
    @PostMapping("/fillAnswer")
    public Result<QuestionVO> fillAnswer(@RequestBody ExerciseFillAnswerFrom exerciseFillAnswerFrom) {
        return iExerciseRecordService.fillAnswer(exerciseFillAnswerFrom);
    }

    /**
     * 分页获取可刷题库列表
     *
     * @param pageNum    页码
     * @param pageSize   每页大小
     * @param title      题库名
     * @param categoryId 分类ID
     * @param subjectId  课程ID
     * @return 响应结果
     */
    @Operation(summary ="分页获取可刷题库列表")
    @GetMapping("/getRepo")
    public Result<IPage<ExerciseRepoVO>> getRepo(
            @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "subjectId", required = false) Integer subjectId) {
        return iRepoService.getRepo(pageNum, pageSize, title, categoryId, subjectId);
    }

    /**
     * 获取单题详情，没有答案
     *
     * @param id 试题id
     * @return
     */
    @Operation(summary ="获取单题详情，没有答案")
    @GetMapping("/question/{id}")
    public Result<QuestionVO> getSingle(@PathVariable("id") Integer id) {
        return iExerciseRecordService.getSingle(id);
    }

    /**
     * 获取用户回答详情
     *
     * @param
     * @return
     */
    @Operation(summary ="获取用户回答详情")
    @GetMapping("/answerInfo/{repoId}/{quId}")
    public Result<AnswerInfoVO> getAnswerInfo(@PathVariable("repoId") Integer repoId, @PathVariable("quId") Integer quId) {
        return iExerciseRecordService.getAnswerInfo(repoId, quId);
    }

    /**
     * 清除用户在指定题库的刷题记录
     *
     * @param repoId 题库Id
     * @return 响应结果
     */
    @Operation(summary ="清除用户在指定题库的刷题记录")
    @DeleteMapping("/clearRecord/{repoId}")
    public Result<String> clearRecord(@PathVariable("repoId") Integer repoId) {
        return iExerciseRecordService.clearRecord(repoId);
    }
}

