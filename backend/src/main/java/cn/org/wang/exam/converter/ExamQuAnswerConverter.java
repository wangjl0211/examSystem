package cn.org.wang.exam.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

import cn.org.wang.exam.config.MapStructConfig;
import cn.org.wang.exam.model.entity.ExamQuAnswer;
import cn.org.wang.exam.model.form.exam_qu_answer.ExamQuAnswerAddForm;

/**
 * @Author Wang
 * @Version
 * @Date 2026/5/6 10:54 AM
 */
@Component
@Mapper(componentModel = "spring", config = MapStructConfig.class)
public interface ExamQuAnswerConverter {

    @Mapping(target = "questionId", source = "quId")
    ExamQuAnswer formToEntity(ExamQuAnswerAddForm examQuAnswerAddForm);

}
