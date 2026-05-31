package cn.org.wang.exam.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.org.wang.exam.mapper.OptionMapper;
import cn.org.wang.exam.model.entity.Option;
import cn.org.wang.exam.service.IOptionService;

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
public class OptionServiceImpl extends ServiceImpl<OptionMapper, Option> implements IOptionService {

}
