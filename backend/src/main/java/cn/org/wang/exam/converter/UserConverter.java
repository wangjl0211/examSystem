package cn.org.wang.exam.converter;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

import cn.org.wang.exam.config.MapStructConfig;
import cn.org.wang.exam.model.entity.User;
import cn.org.wang.exam.model.form.user.UserForm;

import java.util.List;

/**
 * @author Wang
 * @Version 1.0
 * @Date 2026/3/29 15:51
 */
@Component
@Mapper(componentModel = "spring", config = MapStructConfig.class)
public interface UserConverter {

    @Mapping(source = "realName", target = "realName")
    @Mapping(source = "teacherCertNo", target = "teacherCertNo")
    @Mapping(source = "mail", target = "mail")
    User fromToEntity(UserForm userForm);

    List<User> listFromToEntity(List<UserForm> list);

}
