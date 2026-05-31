package cn.org.wang.exam.service;

import java.util.List;
import java.util.Map;

/**
 * 接口信息服务接口
 * 用于获取项目的实际接口信息
 *
 * @author Wang
 * @since 2026-03-21
 */
public interface IApiInfoService {

    /**
     * 获取项目所有接口信息
     * @return 接口信息列表
     */
    List<Map<String, Object>> getAllApiInfo();

    /**
     * 获取接口信息统计
     * @return 接口信息统计
     */
    Map<String, Object> getApiStats();
}
