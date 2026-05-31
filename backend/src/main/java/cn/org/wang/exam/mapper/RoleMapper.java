package cn.org.wang.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import cn.org.wang.exam.model.entity.Role;

import java.util.List;

/**
 * 角色表 Mapper 接口
 *
 * @author Wang
 * @since 2026-03-21
 */
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 获取角色代码
     *
     * @param roleId 角色ID
     * @return 结果集
     */
    List<String> selectCodeById(Integer roleId);

}
