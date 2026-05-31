package cn.org.wang.exam.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;

import cn.org.wang.exam.common.group.AnswerGroup;
import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.model.form.answer.CorrectAnswerFrom;
import cn.org.wang.exam.model.vo.answer.AnswerExamVO;
import cn.org.wang.exam.model.vo.answer.UncorrectedUserVO;
import cn.org.wang.exam.model.vo.answer.UserAnswerDetailVO;
import cn.org.wang.exam.service.IManualScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 答卷管理
 *
 * @author Wang
 * @Version
 * @Date 2026/3/25 11:20 AM
 */
@Tag(name = "答卷管理接口")
@RestController
@RequestMapping("/api/answers")
public class AnswerController {

    @Resource
    private IManualScoreService manualScoreService;

    /**
     * 试卷查询信息
     *
     * @return
     */
    @Operation(summary ="试卷查询信息")
    @GetMapping("/detail")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<List<UserAnswerDetailVO>> getDetail(@RequestParam Integer userId,
                                                      @RequestParam Integer examId) {
        return manualScoreService.getDetail(userId, examId);
    }

    /**
     * 批改试卷
     *
     * @return
     */
    @Operation(summary ="批改试卷")
    @PutMapping("/correct")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> correct(@RequestBody @Validated(AnswerGroup.CorrectGroup.class) List<CorrectAnswerFrom> correctAnswerFroms) {
        return manualScoreService.correct(correctAnswerFroms);
    }

    /**
     * 分页查找待阅卷考试
     *
     * @return
     */
    @Operation(summary ="分页查找待阅卷考试")
    @GetMapping("/exam/page")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<IPage<AnswerExamVO>> examPage(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                                @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                                @RequestParam(value = "examName", required = false) String examName) {
        return manualScoreService.examPage(pageNum, pageSize, examName);
    }

    /**
     * 查询待批阅的用户
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param examId   考试ID
     * @return
     */
    @Operation(summary ="查询待批阅的用户")
    @GetMapping("/exam/stu")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<IPage<UncorrectedUserVO>> stuExamPage(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                                        @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                                        @RequestParam(value = "examId") Integer examId,
                                                        @RequestParam(value = "realName", required = false) String realName) {
        return manualScoreService.stuExamPage(pageNum, pageSize, examId, realName);
    }
}

