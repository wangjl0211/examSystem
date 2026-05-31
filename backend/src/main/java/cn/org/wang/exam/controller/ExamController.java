package cn.org.wang.exam.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;

import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.model.form.exam.ExamAddForm;
import cn.org.wang.exam.model.form.exam.ExamUpdateForm;
import cn.org.wang.exam.model.form.exam_qu_answer.ExamQuAnswerAddForm;
import cn.org.wang.exam.model.vo.exam.*;
import cn.org.wang.exam.model.vo.record.ExamRecordDetailVO;
import cn.org.wang.exam.service.IExamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 考试管理
 *
 * @Author Wang
 * @since 2026-03-21
 */
@Tag(name = "考试管理相关接口")
@RestController
@RequestMapping("/api/exams")
public class ExamController {

    @Resource
    private IExamService examService;

    /**
     * 创建考试
     *
     * @param examAddForm
     * @return
     */
    @Operation(summary = "创建考试")
    @PostMapping
    @PreAuthorize("hasAuthority('role_teacher')")
    public Result<String> createExam(@Validated @RequestBody ExamAddForm examAddForm) {
        return examService.createExam(examAddForm);
    }

    /**
     * 开始考试
     *
     * @param examId 试卷ID
     * @return
     */
    @Operation(summary = "开始考试")
    @GetMapping("/start")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_student')")
    public Result<String> startExam(@RequestParam("examId") @NotNull Integer examId) {
        return examService.startExam(examId);
    }

    /**
     * 修改考试
     *
     * @param examUpdateForm
     * @param id             试卷ID
     * @return
     */
    @Operation(summary = "修改考试")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('role_teacher')")
    public Result<String> updateExam(@Validated @RequestBody ExamUpdateForm examUpdateForm, @PathVariable("id") @NotNull Integer id) {
        return examService.updateExam(examUpdateForm, id);
    }

    /**
     * 删除考试
     *
     * @param ids 试卷ID
     * @return
     */
    @Operation(summary = "删除考试")
    @DeleteMapping("/{ids}")
    @PreAuthorize("hasAuthority('role_teacher')")
    public Result<String> deleteExam(@PathVariable("ids") String ids) {
        // 手动验证：只允许数字和逗号，避免正则表达式栈溢出风险
        if (!isValidIdList(ids)) {
            return Result.failed("无效的ID格式");
        }
        return examService.deleteExam(ids);
    }
    
    /**
     * 验证ID列表格式是否有效
     * @param ids ID列表字符串
     * @return 是否有效
     */
    private boolean isValidIdList(String ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        
        String[] parts = ids.split(",");
        for (String part : parts) {
            if (part.isEmpty()) {
                return false;
            }
            for (char c : part.toCharArray()) {
                if (!Character.isDigit(c)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 教师分页查找考试列表
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param title    试卷标题
     * @return
     */
    @Operation(summary = "教师分页查找考试列表")
    @GetMapping("/paging")
    @PreAuthorize("hasAuthority('role_teacher')")
    public Result<IPage<ExamVO>> getPagingExam(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                               @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                               @RequestParam(value = "title", required = false) String title) {
        return examService.getPagingExam(pageNum, pageSize, title);
    }

    /**
     * 获取考试题目id列表
     *
     * @param examId 试卷ID
     * @return
     */
    @Operation(summary = "获取考试题目id列表")
    @GetMapping("/question/list/{examId}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_student')")
    public Result<ExamQuestionListVO> getQuestionList(@PathVariable("examId") @NotBlank Integer examId) {
        return examService.getQuestionList(examId);
    }

    /**
     * 获取单题信息
     *
     * @param examId     试卷ID
     * @param questionId 试题ID
     * @return
     */
    @Operation(summary = "获取单题信息")
    @GetMapping("/question/single")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_student')")
    public Result<ExamQuDetailVO> getQuestionSingle(@RequestParam("examId") Integer examId,
                                                    @RequestParam("questionId") Integer questionId) {
        return examService.getQuestionSingle(examId, questionId);
    }

    /**
     * 题目汇总
     *
     * @param examId 试卷ID
     * @return
     */
    @Operation(summary = "题目汇总")
    @GetMapping("/collect/{id}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_student')")
    public Result<List<ExamQuCollectVO>> getCollect(@PathVariable("id") @NotNull Integer examId) {
        return examService.getCollect(examId);
    }


    /**
     * 获取考试详情信息
     *
     * @param examId 试卷ID
     * @return
     */
    @Operation(summary = "获取考试详情信息")
    @GetMapping("/detail")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_student')")
    public Result<ExamDetailVO> getDetail(@RequestParam("examId") @NotBlank Integer examId) {
        return examService.getDetail(examId);
    }

    /**
     * 根据课程获得考试
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param title    试卷标题
     * @param isASC    是否升序排列，true为升序，false为降序，默认为false
     * @return
     */
    @Operation(summary = "根据课程获得考试")
    @GetMapping("/subject")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_student')")
    public Result<IPage<ExamsubjectListVO>> getsubjectExamList(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                                           @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                                           @RequestParam(value = "title", required = false) String title,
                                                           @RequestParam(value = "isASC", required = false, defaultValue = "false") Boolean isASC) {
        return examService.getsubjectExamList(pageNum, pageSize, title, isASC);
    }

    /**
     * 考试作弊次数添加
     *
     * @param examId 试卷ID
     * @return
     */
    @Operation(summary = "考试作弊次数添加")
    @PutMapping("/cheat/{examId}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_student')")
    public Result<Integer> addCheat(@PathVariable("examId") @NotNull Integer examId) {
        return examService.addCheat(examId);
    }

    /**
     * 填充答案
     *
     * @param examQuAnswerForm
     * @return
     */
    @Operation(summary = "填充答案")
    @PostMapping("/full-answer")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_student')")
    public Result<String> addAnswer(@Validated @RequestBody ExamQuAnswerAddForm examQuAnswerForm) {
        return examService.addAnswer(examQuAnswerForm);
    }

    /**
     * 交卷操作
     *
     * @param examId 试卷ID
     * @return
     */
    @Operation(summary = "交卷操作")
    @GetMapping(value = "/hand-exam/{examId}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_student')")
    public Result<ExamQuDetailVO> handleExam(@PathVariable("examId") @NotNull Integer examId) {
        return examService.handExam(examId);
    }

    /**
     * 详情
     *
     * @param examId 试卷ID
     * @return
     */
    @Operation(summary = "查看考试详情")
    @GetMapping(value = "/details/{examId}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_student')")
    public Result<List<ExamRecordDetailVO>> details(@PathVariable("examId") @NotNull Integer examId) {
        return examService.details(examId);
    }

    /**
     * 获取服务器时间戳
     * 用于前端定时器时间校准
     *
     * @return 服务器当前时间戳（毫秒）
     */
    @Operation(summary = "获取服务器时间戳")
    @GetMapping("/server-time")
    public Result<Long> getServerTime() {
        return Result.success("获取成功", System.currentTimeMillis());
    }
}