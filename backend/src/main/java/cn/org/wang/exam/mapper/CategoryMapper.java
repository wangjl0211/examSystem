package cn.org.wang.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import cn.org.wang.exam.model.entity.Category;

import org.apache.ibatis.annotations.Mapper;

/**
 * 分类Mapper接口
 *
 * @author Wang
 * @since 2026-04-09
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}