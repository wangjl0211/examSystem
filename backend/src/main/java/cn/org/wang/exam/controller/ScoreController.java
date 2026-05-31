package cn.org.wang.exam.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;

import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.model.vo.score.QuestionAnalyseVO;
import cn.org.wang.exam.model.vo.score.SubjectScoreVO;
import cn.org.wang.exam.model.vo.score.UserScoreVO;
import cn.org.wang.exam.service.IExamQuAnswerService;
import cn.org.wang.exam.service.IUserExamsScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 成绩管理
 *
 * @author Wang
 * @Version
 * @Date 2026/3/25 11:19 AM
 */
@Slf4j
@Tag(name = "成绩相关接口")
@RestController
@RequestMapping("/api/score")
public class ScoreController {

    @Resource
    private IUserExamsScoreService iUserExamsScoreService;
    @Resource
    private IExamQuAnswerService iExamQuAnswerService;

    /**
     * 分页获取成绩信息
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param subjectId  课程Id
     * @param examId   考试Id
     * @param realName 真实姓名
     * @return 响应结果
     */
    @Operation(summary ="分页获取成绩信息")
    @GetMapping("/paging")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<IPage<UserScoreVO>> pagingScore(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                                  @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                                  @RequestParam(value = "subjectId") Integer subjectId,
                                                  @RequestParam(value = "examId") Integer examId,
                                                  @RequestParam(value = "realName", required = false) String realName) {
        return iUserExamsScoreService.pagingScore(pageNum, pageSize, subjectId, examId, realName);
    }

    /**
     * 获取某场考试某题作答情况
     *
     * @param examId     考试id
     * @param questionId 试题id
     * @return 响应结果
     */
    @Operation(summary ="获取某场考试某题作答情况")
    @GetMapping("/question/{examId}/{questionId}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<QuestionAnalyseVO> questionAnalyse(@PathVariable("examId") Integer examId,
                                                     @PathVariable("questionId") Integer questionId) {
        return iExamQuAnswerService.questionAnalyse(examId, questionId);
    }

    /**
     * 根据课程分析考试情况
     *
     * @param pageNum   页码
     * @param pageSize  每页记录数
     * @param examTitle 考试名称
     * @return 响应结果
     */
    @Operation(summary ="根据课程分析考试情况")
    @GetMapping("/getExamScore")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<IPage<SubjectScoreVO>> getExamScoreInfo(
            @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(value = "examTitle", required = false) String examTitle,
            @RequestParam(value = "subjectId", required = false) Integer subjectId) {
        return iUserExamsScoreService.getExamScoreInfo(pageNum, pageSize, examTitle, subjectId);
    }

    /**
     * 成绩导出
     *
     * @param response 响应对象
     * @param examId   考试id
     * @param subjectId  课程id
     */
    @Operation(summary ="成绩导出")
    @GetMapping("/export/{examId}/{subjectId}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public void scoreExport(HttpServletResponse response, @PathVariable("examId") Integer examId, @PathVariable("subjectId") Integer subjectId) {
        try {
            iUserExamsScoreService.exportScores(response, examId, subjectId);
        } catch (Exception e) {
            log.error("成绩导出失败, examId={}, subjectId={}", examId, subjectId, e);
            // 如果响应已提交（数据已开始写入），则无法再修改响应
            if (!response.isCommitted()) {
                response.reset();
                response.setContentType("application/json;charset=utf-8");
                try {
                    response.getWriter().write("{\"code\":0,\"msg\":\"导出失败：" + e.getMessage() + "\"}");
                } catch (java.io.IOException ex) {
                    log.error("写入错误响应失败", ex);
                }
            }
        }
    }

}

