package cn.org.wang.exam.converter;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import cn.org.wang.exam.config.MapStructConfig;
import cn.org.wang.exam.model.entity.Reply;
import cn.org.wang.exam.model.form.reply.ReplyForm;

/**
 * @author Wang
 * @version 1.0
 * @since 2026/4/4 14:11
 */
@Component
@Mapper(componentModel = "spring", config = MapStructConfig.class)
public interface ReplyConverter {
    Reply formToEntity(ReplyForm replyForm);
}
