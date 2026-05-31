package cn.org.wang.exam.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

import cn.org.wang.exam.config.MapStructConfig;
import cn.org.wang.exam.model.entity.Question;
import cn.org.wang.exam.model.form.question.QuestionFrom;
import cn.org.wang.exam.model.vo.exercise.QuestionSheetVO;
import cn.org.wang.exam.model.vo.question.QuestionVO;

import java.util.List;


/**
 * @author Wang
 * @Version 1.0
 * @Date 2026/4/1 15:46
 */
@Component
@Mapper(componentModel = "spring", config = MapStructConfig.class)
public interface QuestionConverter {

    @Mapping(target = "repoId",source = "repoId")
    Question fromToEntity(QuestionFrom questionFrom);

    List<QuestionSheetVO> listEntityToVO(List<Question> questions);

    @Mapping(target = "quId",source = "id")
    QuestionSheetVO entityToVO(Question question);

    QuestionVO questionToQuestionVO(Question question);
}
