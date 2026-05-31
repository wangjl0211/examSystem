package cn.org.wang.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.org.wang.exam.common.exception.ServiceRuntimeException;
import cn.org.wang.exam.mapper.LogMapper;
import cn.org.wang.exam.model.entity.Log;
import cn.org.wang.exam.service.ILogService;
import cn.org.wang.exam.utils.SecurityUtil;

import org.springframework.stereotype.Service;

/**
 * 说明：
 *
 * @Author Wang
 * @Version 1.0
 * @Date 2026/4/4 11:41 AM
 */
@Service
public class LogServiceImpl implements ILogService {
    private final LogMapper logMapper;

    public LogServiceImpl(LogMapper logMapper) {
        this.logMapper = logMapper;
    }

    @Override
    public Log add(Log log) {
        int insert = logMapper.insert(log);
        if(insert>0){
            return log;
        }
        throw new ServiceRuntimeException("添加日志失败");
    }

    @Override
    public Page<Log> getPage(Integer pageNum, Integer pageSize) {
        Integer userId = SecurityUtil.getUserId();
        Page<Log> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Log> query = new LambdaQueryWrapper<>();
        query.eq(Log::getUserId,userId)
                .orderByDesc(Log::getCreateTime);
        page = logMapper.selectPage(page,query);
        return page;
    }
}
