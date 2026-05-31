package cn.org.wang.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import cn.org.wang.exam.common.base.BaseSerializable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 课程实体类
 *
 * @author Wang
 * @since 2026-03-21
 */
@Data
@Schema(description ="课程实体类")
@TableName("t_subject")
@EqualsAndHashCode(callSuper = true)
public class Subject extends BaseSerializable {

    private static final long serialVersionUID = 1L;

    @Schema(description ="课程ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description ="课程名称")
    private String subjectName;

    @Schema(description ="创建人ID")
    @TableField(fill = FieldFill.INSERT)
    private Integer userId;

    @Schema(description ="课程口令")
    private String code;

    @Schema(description ="创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    @Schema(description ="逻辑删除字段")
    private Integer isDeleted;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }
}