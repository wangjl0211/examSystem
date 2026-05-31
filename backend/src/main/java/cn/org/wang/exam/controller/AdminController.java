package cn.org.wang.exam.controller;

import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.model.entity.IpWhitelist;
import cn.org.wang.exam.model.form.auth.LoginForm;
import cn.org.wang.exam.service.IAuthService;
import cn.org.wang.exam.service.IIpWhitelistService;
import cn.org.wang.exam.utils.IPUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员控制器
 * 处理管理员登录和IP白名单管理
 *
 * @author Wang
 * @Version 1.0
 * @Date 2026/5/16
 */
@Tag(name = "管理员接口")
@RestController
@RequestMapping("/api/admin")
@Slf4j
@PreAuthorize("hasAuthority('role_admin')")  // 类级别权限限制：仅管理员可访问
public class AdminController {

    @Resource
    private IAuthService iAuthService;

    @Resource
    private IIpWhitelistService iIpWhitelistService;

    /**
     * 管理员登录接口
     * 仅允许白名单IP访问
     * 注意：登录接口不需要管理员权限，使用 @PreAuthorize("permitAll()") 覆盖类级别限制
     *
     * @param request   请求对象
     * @param loginForm 登录表单
     * @return Token
     */
    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    @PreAuthorize("permitAll()")  // 登录接口允许所有访问
    public Result<String> adminLogin(HttpServletRequest request,
                                     @Validated @RequestBody LoginForm loginForm) {
        // 获取客户端IP
        String clientIp = IPUtils.getIPAddress(request);
        log.info("管理员登录请求 - 客户端IP: {}", clientIp);

        // 校验IP是否在白名单中
        if (!iIpWhitelistService.isIpAllowed(clientIp)) {
            log.warn("管理员登录被拒绝 - IP {} 不在白名单中", clientIp);
            return Result.failed("Access denied: IP not in whitelist");
        }

        // 调用管理员登录服务
        return iAuthService.adminLogin(request, loginForm);
    }

    /**
     * 获取IP白名单列表
     *
     * @return IP白名单列表
     */
    @Operation(summary = "获取IP白名单列表")
    @GetMapping("/whitelist/list")
    public Result<List<IpWhitelist>> getWhitelistList() {
        List<IpWhitelist> list = iIpWhitelistService.list();
        return Result.success("获取成功", list);
    }

    /**
     * 分页获取IP白名单列表
     *
     * @param page 页码
     * @param size 每页数量
     * @return 分页结果
     */
    @Operation(summary = "分页获取IP白名单列表")
    @GetMapping("/whitelist/page")
    public Result<Page<IpWhitelist>> getWhitelistPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<IpWhitelist> pageParam = new Page<>(page, size);
        Page<IpWhitelist> result = iIpWhitelistService.page(pageParam);
        return Result.success("获取成功", result);
    }

    /**
     * 根据ID获取IP白名单详情
     *
     * @param id 规则ID
     * @return IP白名单详情
     */
    @Operation(summary = "获取IP白名单详情")
    @GetMapping("/whitelist/{id}")
    public Result<IpWhitelist> getWhitelistById(@PathVariable Integer id) {
        IpWhitelist ipWhitelist = iIpWhitelistService.getById(id);
        if (ipWhitelist == null) {
            return Result.failed("规则不存在");
        }
        return Result.success("获取成功", ipWhitelist);
    }

    /**
     * 添加IP白名单规则
     *
     * @param ipWhitelist IP白名单实体
     * @return 操作结果
     */
    @Operation(summary = "添加IP白名单规则")
    @PostMapping("/whitelist")
    public Result<String> addWhitelist(@Valid @RequestBody IpWhitelist ipWhitelist) {
        return iIpWhitelistService.addRule(ipWhitelist);
    }

    /**
     * 更新IP白名单规则
     *
     * @param ipWhitelist IP白名单实体
     * @return 操作结果
     */
    @Operation(summary = "更新IP白名单规则")
    @PutMapping("/whitelist")
    public Result<String> updateWhitelist(@Valid @RequestBody IpWhitelist ipWhitelist) {
        return iIpWhitelistService.updateRule(ipWhitelist);
    }

    /**
     * 删除IP白名单规则
     *
     * @param id 规则ID
     * @return 操作结果
     */
    @Operation(summary = "删除IP白名单规则")
    @DeleteMapping("/whitelist/{id}")
    public Result<String> deleteWhitelist(@PathVariable Integer id) {
        return iIpWhitelistService.deleteRule(id);
    }

    /**
     * 启用/禁用IP白名单规则
     *
     * @param id     规则ID
     * @param status 状态：1启用 0禁用
     * @return 操作结果
     */
    @Operation(summary = "启用/禁用IP白名单规则")
    @PutMapping("/whitelist/{id}/status")
    public Result<String> toggleWhitelistStatus(@PathVariable Integer id,
                                                 @RequestParam Integer status) {
        return iIpWhitelistService.toggleRule(id, status);
    }

    /**
     * 刷新IP白名单缓存
     *
     * @return 操作结果
     */
    @Operation(summary = "刷新IP白名单缓存")
    @PostMapping("/whitelist/refresh")
    public Result<String> refreshWhitelistCache() {
        iIpWhitelistService.refreshCache();
        return Result.success("缓存刷新成功");
    }

    /**
     * 获取所有启用的IP白名单规则
     *
     * @return IP白名单列表
     */
    @Operation(summary = "获取所有启用的IP白名单规则")
    @GetMapping("/whitelist/enabled")
    public Result<List<IpWhitelist>> getEnabledWhitelist() {
        List<IpWhitelist> list = iIpWhitelistService.getAllEnabledRules();
        return Result.success("获取成功", list);
    }
}
