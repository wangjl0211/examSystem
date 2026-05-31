package cn.org.wang.exam.utils.security;


import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;
import lombok.AccessLevel;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import cn.org.wang.exam.model.entity.User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Spring Security 用户对象
 *
 * @author Wang
 * @since 3.0.0
 */
@NoArgsConstructor
public class SysUserDetails implements UserDetails {
    @Getter(AccessLevel.NONE)
    private List<SimpleGrantedAuthority> permissions;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private User user;

    /**
     * 获取用户信息
     * 注意：返回内部对象引用，调用方不应修改返回对象的状态
     *
     * @return 用户实体对象
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Spring Security 框架需要访问用户信息，且 User 对象在认证上下文中通常是只读的")
    public User getUser() { return user; }

    /**
     * 设置用户信息
     *
     * @param user 用户实体对象，不能为null
     * @throws NullPointerException 如果 user 为 null
     */
    public void setUser(User user) { this.user = Objects.requireNonNull(user, "用户信息不能为null"); }

    public List<SimpleGrantedAuthority> getPermissions() {
        return permissions == null ? Collections.emptyList() : List.copyOf(permissions);
    }

    public SysUserDetails(User user) {
        this.user = Objects.requireNonNull(user, "用户信息不能为null");
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return permissions == null ? Collections.emptyList() : Collections.unmodifiableList(permissions);
    }

    public void setPermissions(List<SimpleGrantedAuthority> permissions) {
        this.permissions = permissions == null ? Collections.emptyList() : List.copyOf(permissions);
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUserNo();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 修复P1安全漏洞：检查用户状态，status=1表示正常，status=0表示禁用
        return user != null && user.getStatus() != null && user.getStatus() == 1;
    }
}
