package cn.org.wang.exam.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.org.wang.exam.config.MapStructConfig;
import cn.org.wang.exam.model.entity.Subject;
import cn.org.wang.exam.model.form.subject.SubjectForm;
import cn.org.wang.exam.model.vo.subject.SubjectVO;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author Wang
 * @Version
 * @Date 2026/3/28 2:03 PM
 */
@Component
@Mapper(componentModel="spring", config = MapStructConfig.class)
public interface SubjectConverter {

    Page<SubjectVO> pageEntityToVo(Page<Subject> page);

    Subject formToEntity(SubjectForm subjectForm);

    List<SubjectVO> listEntityToVo(List<Subject> page);
    SubjectVO  subjectTosubjectVO(Subject subject);

}
