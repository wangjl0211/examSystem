package cn.org.wang.exam.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

import cn.org.wang.exam.config.MapStructConfig;
import cn.org.wang.exam.model.entity.ExerciseRecord;
import cn.org.wang.exam.model.form.exercise.ExerciseFillAnswerFrom;
import cn.org.wang.exam.model.vo.exercise.AnswerInfoVO;
import cn.org.wang.exam.model.vo.question.QuestionVO;

/**
 * @author Wang
 * @Version 1.0
 * @Date 2026/5/6 10:15
 */
@Component
@Mapper(componentModel="spring", config = MapStructConfig.class)
public interface ExerciseConverter {
    @Mapping(source = "quId",target = "questionId")
    @Mapping(source = "quType",target = "questionType")
    ExerciseRecord fromToEntity(ExerciseFillAnswerFrom exerciseFillAnswerFrom);

    AnswerInfoVO quVOToAnswerInfoVO(QuestionVO questionVO);
}
