package cn.org.wang.exam.mapper;

import cn.org.wang.exam.model.entity.IpWhitelist;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * IP白名单数据映射层
 *
 * @author Wang
 * @Version 1.0
 * @Date 2026/5/16
 */
@Mapper
public interface IpWhitelistMapper extends BaseMapper<IpWhitelist> {
}
