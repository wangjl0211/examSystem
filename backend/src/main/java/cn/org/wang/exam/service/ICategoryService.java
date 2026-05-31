package cn.org.wang.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;

import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.model.entity.Category;
import cn.org.wang.exam.model.vo.category.CategoryVO;

import java.util.List;

/**
 * 分类服务接口
 *
 * @author Wang
 * @since 2026-04-09
 */
public interface ICategoryService extends IService<Category> {

    /**
     * 添加分类
     *
     * @param category 分类信息
     * @return 结果
     */
    Result<String> addCategory(Category category);

    /**
     * 修改分类
     *
     * @param category 分类信息
     * @param id 分类ID
     * @return 结果
     */
    Result<String> updateCategory(Category category, Integer id);

    /**
     * 删除分类
     *
     * @param id 分类ID
     * @return 结果
     */
    Result<String> deleteCategory(Integer id);

    /**
     * 获取分类树
     *
     * @return 分类树
     */
    Result<List<CategoryVO>> getCategoryTree();

    /**
     * 获取一级分类列表
     *
     * @return 一级分类列表
     */
    Result<List<CategoryVO>> getFirstLevelCategories();

    /**
     * 根据父ID获取子分类
     *
     * @param parentId 父ID
     * @return 子分类列表
     */
    Result<List<CategoryVO>> getChildCategories(Integer parentId);
}