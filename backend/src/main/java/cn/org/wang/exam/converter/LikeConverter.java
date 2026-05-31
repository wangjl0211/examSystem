package cn.org.wang.exam.converter;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import cn.org.wang.exam.config.MapStructConfig;
import cn.org.wang.exam.model.entity.Like;
import cn.org.wang.exam.model.form.like.LikeForm;

/**
 * @author Wang
 * @version 1.0
 * @since 2026/4/16 22:18
 */
@Component
@Mapper(componentModel = "spring", config = MapStructConfig.class)
public interface LikeConverter {
    Like formToEntity(LikeForm likeForm);
}
