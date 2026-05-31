package cn.org.wang.exam.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 选项实体类
 *
 * @author Wang
 * @since 2026-03-21
 */
@Data
@Schema(description ="选项实体类")
@TableName("t_option")
public class Option implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description ="选项ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description ="试题ID")
    private Integer quId;

    @Schema(description ="是否正确")
    @NotNull(message = "选型是否正确(isRight)不能为空")
    @Min(value = 0,message = "选项是否正确(isRight)只能是：0错误1正确")
    @Max(value = 1,message = "选项是否正确(isRight)只能是：0错误1正确")
    private Integer isRight;

    /**
     * 0错误 1正确
     */
    @Schema(description ="图片地址")
    private String image;

    @Schema(description ="选项内容")
    @NotBlank(message = "选型内容(content)不能为空")
    private String content;

    @Schema(description ="排序")
    private Integer sort;

    @TableLogic
    @Schema(description ="逻辑删除字段")
    private Integer isDeleted;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getQuId() { return quId; }
    public void setQuId(Integer quId) { this.quId = quId; }
    public Integer getIsRight() { return isRight; }
    public void setIsRight(Integer isRight) { this.isRight = isRight; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }
}

