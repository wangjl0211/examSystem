package cn.org.wang.exam.service;

import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.model.entity.IpWhitelist;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * IP白名单服务接口
 *
 * @author Wang
 * @Version 1.0
 * @Date 2026/5/16
 */
public interface IIpWhitelistService extends IService<IpWhitelist> {

    /**
     * 校验IP是否在白名单中
     *
     * @param clientIp 客户端IP地址
     * @return true-在白名单中，false-不在白名单中
     */
    boolean isIpAllowed(String clientIp);

    /**
     * 获取所有启用的IP白名单规则
     *
     * @return IP白名单列表
     */
    List<IpWhitelist> getAllEnabledRules();

    /**
     * 添加IP白名单规则
     *
     * @param ipWhitelist IP白名单实体
     * @return 操作结果
     */
    Result<String> addRule(IpWhitelist ipWhitelist);

    /**
     * 更新IP白名单规则
     *
     * @param ipWhitelist IP白名单实体
     * @return 操作结果
     */
    Result<String> updateRule(IpWhitelist ipWhitelist);

    /**
     * 删除IP白名单规则（逻辑删除）
     *
     * @param id 规则ID
     * @return 操作结果
     */
    Result<String> deleteRule(Integer id);

    /**
     * 启用/禁用IP白名单规则
     *
     * @param id     规则ID
     * @param status 状态：1启用 0禁用
     * @return 操作结果
     */
    Result<String> toggleRule(Integer id, Integer status);

    /**
     * 刷新IP白名单缓存
     */
    void refreshCache();
}
