package cn.org.wang.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import cn.org.wang.exam.model.entity.Log;

import org.apache.ibatis.annotations.Mapper;

/**
 * 说明：
 * 日志表Mapper
 *
 * @Author Wang
 * @Version 1.0
 * @Date 2026/4/4 2:55 PM
 */
@Mapper
public interface LogMapper extends BaseMapper<Log> {
}
