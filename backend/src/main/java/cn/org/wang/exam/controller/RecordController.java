package cn.org.wang.exam.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.model.vo.record.ExamRecordDetailWithTimeVO;
import cn.org.wang.exam.service.IExerciseRecordService;

/**
 * 考试记录
 *
 * @Author Wang
 * @Version
 * @Date 2026/3/25 11:22 AM
 */
@Tag(name = "考试记录相关接口")
@RestController
@RequestMapping("/api/records")
public class RecordController {
    @Resource
    private IExerciseRecordService exerciseRecordService;

    /**
     * 查询试卷详情
     *
     * @param examId
     * @return
     */
    @Operation(summary ="查询试卷详情")
    @GetMapping("/exam/detail")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<ExamRecordDetailWithTimeVO> getExamRecordDetail(@RequestParam(value = "examId",required = true) Integer examId,
                                                                @RequestParam(value = "userId",required = false)Integer userId) {
        return exerciseRecordService.getExamRecordDetail(examId,userId);
    }


}

