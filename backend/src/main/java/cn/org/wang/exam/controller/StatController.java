package cn.org.wang.exam.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.model.vo.stat.AllStatsVO;
import cn.org.wang.exam.model.vo.stat.DailyVO;
import cn.org.wang.exam.model.vo.stat.SubjectExamVO;
import cn.org.wang.exam.model.vo.stat.SubjectStudentVO;
import cn.org.wang.exam.service.IStatService;

import java.util.List;
import java.util.Map;


/**
 * 统计管理
 *
 * @Author Wang
 * @Version 1.0
 * @Date 2026/3/25 11:22 AM
 */
@Tag(name = "统计数据相关接口")
@RestController
@RequestMapping("/api/stat")
public class StatController {

    @Resource
    private IStatService statService;

    /**
     * 各课程人数统计
     *
     * @return
     */
    @Operation(summary ="各课程人数统计")
    @GetMapping("/student")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<List<SubjectStudentVO>> getStudentSubjectCount() {
        return statService.getStudentSubjectCount();
    }

    /**
     * 各班试卷统计
     *
     * @return
     */
    @Operation(summary ="各班试卷统计")
    @GetMapping("/exam")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<List<SubjectExamVO>> getExamSubjectCount() {
        return statService.getExamSubjectCount();
    }

    /**
     * 统计所有课程、试卷、试题数量
     *
     * @return 统计结果
     */
    @Operation(summary ="统计所有课程、试卷、试题数量")
    @GetMapping("/allCounts")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<AllStatsVO> getAllCount() {
        return statService.getAllCount();
    }

    /**
     * 获取用户登录时间统计
     *
     * @return
     */
    @Operation(summary ="获取用户登录时间统计")
    @GetMapping("/daily")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<List<DailyVO>> getDaily() {
        return statService.getDaily();
    }

    /**
     * 获取管理员主页数据
     *
     * @return
     */
    @Operation(summary ="获取管理员主页数据")
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyAuthority('role_admin')")
    public Result<Map<String, Object>> getDashboard() {
        return statService.getDashboard();
    }

}

