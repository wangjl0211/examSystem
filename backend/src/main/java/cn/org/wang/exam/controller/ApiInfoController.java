package cn.org.wang.exam.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.service.IApiInfoService;

import java.util.List;
import java.util.Map;

/**
 * 接口信息控制器
 * 用于获取项目的实际接口信息
 *
 * @author Wang
 * @since 2026-03-21
 */
@Slf4j
@Tag(name = "接口信息管理")
@RestController
@RequestMapping("/api/info")
@PreAuthorize("hasAuthority('role_admin')")  // 修复P0安全漏洞：仅管理员可访问
public class ApiInfoController {

    @Resource
    private IApiInfoService apiInfoService;

    /**
     * 获取项目所有接口信息
     * @return 接口信息列表
     */
    @Operation(summary = "获取项目所有接口信息")
    @GetMapping("/apis")
    public Result<List<Map<String, Object>>> getAllApiInfo() {
        try {
            List<Map<String, Object>> apiInfoList = apiInfoService.getAllApiInfo();
            return Result.success("获取接口信息成功", apiInfoList);
        } catch (Exception e) {
            // 修复P0安全漏洞：不暴露异常详情
            log.error("获取接口信息失败", e);
            return Result.failed("获取接口信息失败，请联系管理员");
        }
    }

    /**
     * 获取接口信息统计
     * @return 接口信息统计
     */
    @Operation(summary = "获取接口信息统计")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getApiStats() {
        try {
            Map<String, Object> stats = apiInfoService.getApiStats();
            return Result.success("获取接口信息统计成功", stats);
        } catch (Exception e) {
            // 修复P0安全漏洞：不暴露异常详情
            log.error("获取接口信息统计失败", e);
            return Result.failed("获取接口信息统计失败，请联系管理员");
        }
    }
}
