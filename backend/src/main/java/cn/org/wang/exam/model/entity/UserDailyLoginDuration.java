package cn.org.wang.exam.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 用户每日登录时长实体类
 *
 * @Author Wang
 * @Version
 * @Date 2026/5/28 10:44 PM
 */
@Data
@Schema(description ="用户每日登录时长实体类")
@TableName("t_user_daily_login_duration")
public class UserDailyLoginDuration implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description ="用户每日登录时长ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description ="用户ID")
    @TableField("user_id")
    private Integer userId;

    @Schema(description ="登录日期")
    @TableField("login_date")
    private LocalDate loginDate;

    @Schema(description ="累积在线秒数")
    @TableField("total_seconds")
    private Integer totalSeconds;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public LocalDate getLoginDate() { return loginDate; }
    public void setLoginDate(LocalDate loginDate) { this.loginDate = loginDate; }
    public Integer getTotalSeconds() { return totalSeconds; }
    public void setTotalSeconds(Integer totalSeconds) { this.totalSeconds = totalSeconds; }
}
