package cn.org.wang.exam.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.org.wang.exam.model.entity.Log;

/**
 * 说明：
 * 日志记录服务（主要记录登录日志）
 *
 * @Author Wang
 * @Version 1.0
 * @Date 2026/4/4 11:38 AM
 */
public interface ILogService {
    /**
     * 记录登录日志
     * @param log
     * @return
     */
    Log add(Log log);

    /**
     * 分页查询登录日志
     * @param pageNum
     * @param pageSize
     * @return
     */
    Page<Log> getPage(Integer pageNum, Integer pageSize);
}
