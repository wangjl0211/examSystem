package cn.org.wang.exam.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.model.entity.Log;
import cn.org.wang.exam.service.ILogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 说明：
 *
 * @Author Wang
 * @Version 1.0
 * @Date 2026/4/4 11:37 AM
 */
@RestController
@Tag(name = "日志记录接口")
@RequestMapping("/api/log")
@PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")  // 所有角色可查看自己的登录日志
public class LogController {
    private final ILogService logService;

    // 构造器注入
    public LogController(ILogService logService) {
        this.logService = logService;
    }

    @GetMapping
    @Operation(summary ="分页查询日志")
    Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<Log>> getLogPage(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                      @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize){
        Page<Log> page = logService.getPage(pageNum, pageSize);
        return Result.success("分页查询日志成功",page);
    }
}

