package cn.org.wang.exam.model.vo.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类VO
 *
 * @author Wang
 * @since 2026-04-09
 */
@Data
@Schema(description = "分类视图对象")
public class CategoryVO {

    @Schema(description = "分类ID")
    private Integer id;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "父分类ID")
    private Integer parentId;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "子分类列表")
    private List<CategoryVO> children;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getParentId() { return parentId; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public List<CategoryVO> getChildren() { return children == null ? null : List.copyOf(children); }
    public void setChildren(List<CategoryVO> children) { this.children = children; }
}
