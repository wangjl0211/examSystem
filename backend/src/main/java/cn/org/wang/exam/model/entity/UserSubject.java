package cn.org.wang.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户课程关联实体类
 *
 * @Author Wang
 * @Version
 * @Date 2026/3/14 6:57 PM
 */
@TableName("t_user_subject")
@Data
public class UserSubject {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description ="用户课程关联表ID")
    private Integer id;

    @Schema(description ="用户ID")
    @TableField("user_id")
    private Integer uId;

    @Schema(description ="课程ID")
    @TableField("subject_id")
    private Integer gId;

    @TableLogic
    @Schema(description ="逻辑删除字段")
    @TableField("is_deleted")
    private Integer isDeleted;

    @Schema(description ="加入时间")
    @TableField("join_time")
    private LocalDateTime joinTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUId() { return uId; }
    public void setUId(Integer uId) { this.uId = uId; }
    public Integer getGId() { return gId; }
    public void setGId(Integer gId) { this.gId = gId; }
    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }
    public LocalDateTime getJoinTime() { return joinTime; }
    public void setJoinTime(LocalDateTime joinTime) { this.joinTime = joinTime; }
}