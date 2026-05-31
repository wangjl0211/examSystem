package cn.org.wang.exam.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import cn.org.wang.exam.common.exception.ServiceRuntimeException;
import cn.org.wang.exam.utils.security.SysUserDetails;

import java.util.List;

/**
 * Security工具类
 * 修复P0问题：添加null检查防止NPE
 *
 * @author Wang
 * @Version 1.0
 * @Date 2026/3/30 0:10
 */
@Slf4j
public class SecurityUtil {

    private SecurityUtil() {
    }

    /**
     * 获取当前用户id
     * 修复：添加null检查防止NPE
     *
     * @return 用户id
     */
    public static Integer getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ServiceRuntimeException("用户未登录");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof SysUserDetails)) {
            throw new ServiceRuntimeException("无法获取用户信息");
        }
        SysUserDetails user = (SysUserDetails) principal;
        if (user.getUser() == null) {
            throw new ServiceRuntimeException("用户信息为空");
        }
        return user.getUser().getId();
    }

    /**
     * 获取当前用户角色
     * 修复：添加null检查和空列表检查
     *
     * @return 角色
     */
    public static String getRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ServiceRuntimeException("用户未登录");
        }
        List<? extends GrantedAuthority> list = authentication.getAuthorities().stream().toList();
        if (list.isEmpty()) {
            throw new ServiceRuntimeException("无法获取角色信息");
        }
        return list.get(0).toString();
    }

    /**
     * 获取当前用户角色代码 1：教师、2：学生、0管理员
     * 修复：添加null检查和空列表检查
     *
     * @return 角色
     */
    public static Integer getRoleCode() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ServiceRuntimeException("用户未登录");
        }
        List<? extends GrantedAuthority> list = authentication.getAuthorities().stream().toList();
        if (list.isEmpty()) {
            throw new ServiceRuntimeException("无法获取角色信息");
        }
        String roleName = list.get(0).toString();
        Integer roleCode;
        if ("role_admin".equals(roleName)) {
            roleCode = 0;
        } else if ("role_teacher".equals(roleName)) {
            roleCode = 1;
        } else if ("role_student".equals(roleName)) {
            roleCode = 2;
        } else {
            throw new ServiceRuntimeException("无法获取角色代码");
        }
        return roleCode;
    }

    /**
     * 当前用户所在课程Id
     */
    public static final Integer SUBJECT_ID = 0;
    
    /**
     * 获取当前用户所在课程Id
     *
     * @return
     */
    public static Integer getSubjectId() {
        return SUBJECT_ID;
    }


}
