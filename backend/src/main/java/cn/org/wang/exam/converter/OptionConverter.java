package cn.org.wang.exam.converter;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import cn.org.wang.exam.config.MapStructConfig;

/**
 * @author Wang
 * @Version 1.0
 * @Date 2026/4/11 14:20
 */
@Component
@Mapper(componentModel="spring", config = MapStructConfig.class)
public interface OptionConverter {

}
