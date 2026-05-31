package cn.org.wang.exam.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 分类实体类
 *
 * @author Wang
 * @since 2026-04-09
 */
@Data
@TableName("t_category")
@Schema(description = "题库分类")
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "分类ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "分类名称")
    @NotBlank(message = "分类名称不能为空")
    private String categoryName;

    @Schema(description = "父分类ID，0表示一级分类")
    private Integer parentId = 0;

    @Schema(description = "排序")
    private Integer sort = 0;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "是否删除")
    @TableLogic
    private Integer isDeleted = 0;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public Integer getParentId() { return parentId; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }
}
