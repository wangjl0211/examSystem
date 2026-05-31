package cn.org.wang.exam.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.model.entity.Category;
import cn.org.wang.exam.model.vo.category.CategoryVO;
import cn.org.wang.exam.service.ICategoryService;

import java.util.List;

/**
 * 分类管理控制器
 *
 * @author Wang
 * @since 2026-04-09
 */
@Tag(name = "题库分类管理相关接口")
@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Resource
    private ICategoryService categoryService;

    /**
     * 添加分类
     *
     * @param category 分类信息
     * @return 结果
     */
    @PostMapping
    @Operation(summary = "添加分类")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> addCategory(@Validated @RequestBody Category category) {
        return categoryService.addCategory(category);
    }

    /**
     * 修改分类
     *
     * @param category 分类信息
     * @param id 分类ID
     * @return 结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "修改分类")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> updateCategory(@Validated @RequestBody Category category, @PathVariable("id") Integer id) {
        return categoryService.updateCategory(category, id);
    }

    /**
     * 删除分类
     *
     * @param id 分类ID
     * @return 结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> deleteCategory(@PathVariable("id") Integer id) {
        return categoryService.deleteCategory(id);
    }

    /**
     * 获取分类树
     *
     * @return 分类树
     */
    @GetMapping("/tree")
    @Operation(summary = "获取分类树")
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<List<CategoryVO>> getCategoryTree() {
        return categoryService.getCategoryTree();
    }

    /**
     * 获取一级分类
     *
     * @return 一级分类列表
     */
    @GetMapping("/first-level")
    @Operation(summary = "获取一级分类")
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<List<CategoryVO>> getFirstLevelCategories() {
        return categoryService.getFirstLevelCategories();
    }

    /**
     * 获取子分类
     *
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    @GetMapping("/children/{parentId}")
    @Operation(summary = "获取子分类")
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<List<CategoryVO>> getChildCategories(@PathVariable("parentId") Integer parentId) {
        return categoryService.getChildCategories(parentId);
    }
}