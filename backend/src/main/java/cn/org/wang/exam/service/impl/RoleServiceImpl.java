package cn.org.wang.exam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.org.wang.exam.mapper.RoleMapper;
import cn.org.wang.exam.model.entity.Role;
import cn.org.wang.exam.service.IRoleService;

import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Wang
 * @since 2026-03-21
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {

}
