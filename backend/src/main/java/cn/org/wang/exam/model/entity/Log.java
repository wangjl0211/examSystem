package cn.org.wang.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 说明：
 *
 * @Author Wang
 * @Version 1.0
 * @Date 2026/4/4 2:50 PM
 */
@Data
@Schema(description ="日志")
@TableName("t_log")
@Builder
public class Log {
    @Schema(description ="id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description ="登录地点")
    private String place;

    @Schema(description ="操作行为")
    private String behavior;

    @Schema(description ="登录设备")
    private String device;

    @Schema(description ="创建用户")
    private Integer userId;

    @Schema(description ="创建时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    public Log() {}

    public Log(Integer id, String place, String behavior, String device, Integer userId, LocalDateTime createTime) {
        this.id = id;
        this.place = place;
        this.behavior = behavior;
        this.device = device;
        this.userId = userId;
        this.createTime = createTime;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getPlace() { return place; }
    public void setPlace(String place) { this.place = place; }
    public String getBehavior() { return behavior; }
    public void setBehavior(String behavior) { this.behavior = behavior; }
    public String getDevice() { return device; }
    public void setDevice(String device) { this.device = device; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public static LogBuilder builder() {
        return new LogBuilder();
    }

    public static class LogBuilder {
        private Integer id;
        private String place;
        private String behavior;
        private String device;
        private Integer userId;
        private LocalDateTime createTime;

        public LogBuilder id(Integer id) { this.id = id; return this; }
        public LogBuilder place(String place) { this.place = place; return this; }
        public LogBuilder behavior(String behavior) { this.behavior = behavior; return this; }
        public LogBuilder device(String device) { this.device = device; return this; }
        public LogBuilder userId(Integer userId) { this.userId = userId; return this; }
        public LogBuilder createTime(LocalDateTime createTime) { this.createTime = createTime; return this; }

        public Log build() {
            Log log = new Log();
            log.setId(id);
            log.setPlace(place);
            log.setBehavior(behavior);
            log.setDevice(device);
            log.setUserId(userId);
            log.setCreateTime(createTime);
            return log;
        }
    }
}

