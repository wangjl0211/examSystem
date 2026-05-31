package cn.org.wang.exam.converter;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import cn.org.wang.exam.config.MapStructConfig;
import cn.org.wang.exam.model.entity.Discussion;
import cn.org.wang.exam.model.form.discussion.DiscussionForm;

/**
 * @author Wang
 * @version 1.0
 * @since 2026/4/3 9:36
 */
@Component
@Mapper(componentModel="spring", config = MapStructConfig.class)
public interface DiscussionConverter {

    Discussion formToEntity(DiscussionForm discussion);
}
