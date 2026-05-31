package cn.org.wang.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import cn.org.wang.exam.common.base.BaseSerializable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 试题实体类
 *
 * @author Wang
 * @since 2026-03-21
 */
@Data
@Schema(description ="试题实体类")
@TableName("t_question")
@EqualsAndHashCode(callSuper = true)
public class Question extends BaseSerializable {

    private static final long serialVersionUID = 1L;

    @Schema(description ="试题ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description ="试题类型")
    private Integer quType;

    @Schema(description ="试题图片")
    private String image;

    @Schema(description ="题干")
    private String content;

    @Schema(description ="创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description ="题目分析")
    private String analysis;

    @Schema(description ="题库ID")
    private Integer repoId;

    @Schema(description ="创建人ID")
    @TableField(fill = FieldFill.INSERT)
    private Integer userId;

    @TableLogic
    @Schema(description ="逻辑删除字段")
    private Integer isDeleted;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getQuType() {
        return quType;
    }

    public void setQuType(Integer quType) {
        this.quType = quType;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }

    public Integer getRepoId() {
        return repoId;
    }

    public void setRepoId(Integer repoId) {
        this.repoId = repoId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }
}

