package cn.org.wang.exam.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;

import cn.org.wang.exam.common.group.UserGroup;
import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.model.form.auth.LoginForm;
import cn.org.wang.exam.model.form.user.UserForm;
import cn.org.wang.exam.model.vo.user.UserVO;
import cn.org.wang.exam.service.IAuthService;
import cn.org.wang.exam.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户管理
 *
 * @author Wang
 * @Version 1.0
 * @Date 2026/3/25 15:50
 */
@Tag(name = "用户管理相关接口")
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private IUserService iUserService;

    @Resource
    private IAuthService iAuthService;

    /**
     * 普通用户登录接口
     * 公开访问接口，允许任意IP地址发起请求
     *
     * @param request   请求对象
     * @param loginForm 登录表单
     * @return Token
     */
    @Operation(summary = "普通用户登录")
    @PostMapping("/login")
    public Result<String> login(HttpServletRequest request,
                                @Validated @RequestBody LoginForm loginForm) {
        return iAuthService.login(request, loginForm);
    }

    /**
     * 获取用户个人信息
     *
     * @return
     */
    @Operation(summary = "获取用户个人信息")
    @GetMapping("/info")
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<UserVO> info() {
        return iUserService.info();
    }




    /**
     * 用户修改密码
     *
     * @param userForm
     * @return
     */
    @Operation(summary = "用户修改密码")
    @PutMapping
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<String> updatePassword(@Validated(UserGroup.UpdatePasswordGroup.class) @RequestBody UserForm userForm) {
        return iUserService.updatePassword(userForm);
    }

    /**
     * 批量删除用户
     *
     * @param ids 删除用户ID
     * @return
     */
    @Operation(summary = "批量删除用户")
    @DeleteMapping("/{ids}")
    @PreAuthorize("hasAnyAuthority('role_admin')")
    public Result<String> deleteBatchByIds(@PathVariable("ids") String ids) {
        return iUserService.deleteBatchByIds(ids);
    }




    /**
     * 用户加入课程，只有学生才能加入课程
     *
     * @param code 课程代码
     * @return
     */
    @Operation(summary = "用户加入课程")
    @PutMapping("/subject/join")
    @PreAuthorize("hasAnyAuthority('role_student')")
    public Result<String> joinsubject(@RequestParam("code") String code) {
        return iUserService.joinsubject(code);
    }

    /**
     * 管理员 用户管理 分页获取用户信息
     *
     * @param pageNum    页码
     * @param pageSize   每页大小
     * @param userNo     学号/工号
     * @param realName   真实姓名
     * @param createDate 注册日期
     * @param roleId     角色ID
     * @return
     */
    @Operation(summary = "分页获取用户信息")
    @GetMapping("/paging")
    @PreAuthorize("hasAnyAuthority('role_admin')")
    public Result<IPage<UserVO>> pagingUser(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                            @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize,
                                            @RequestParam(value = "userNo", required = false) String userNo,
                                            @RequestParam(value = "realName", required = false) String realName,
                                            @RequestParam(value = "createDate", required = false) String createDate,
                                            @RequestParam(value = "roleId", required = false) Integer roleId) {
        return iUserService.pagingUser(pageNum, pageSize, userNo, realName, createDate, roleId);
    }

    /**
     * 用户上传头像
     *
     * @param file 头像文件
     * @return 返回头像地址
     */
    @Operation(summary = "用户上传头像")
    @PutMapping("/uploadAvatar")
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<String> uploadAvatar(@RequestPart("file") MultipartFile file) {
        return iUserService.uploadAvatar(file);
    }
}
