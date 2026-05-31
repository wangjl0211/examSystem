package cn.org.wang.exam.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.org.wang.exam.config.MapStructConfig;
import cn.org.wang.exam.model.entity.Exam;
import cn.org.wang.exam.model.entity.ExamQuestion;
import cn.org.wang.exam.model.entity.Option;
import cn.org.wang.exam.model.form.exam.ExamAddForm;
import cn.org.wang.exam.model.form.exam.ExamUpdateForm;
import cn.org.wang.exam.model.vo.exam.*;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author Wang
 * @Version
 * @Date 2026/4/1 3:18 PM
 */
@Component
@Mapper(componentModel="spring", config = MapStructConfig.class)
public interface ExamConverter {

    Page<ExamVO> pageEntityToVo(Page<Exam> examPage);

    Exam  formToEntity(ExamUpdateForm examUpdateForm);

    Exam  formToEntity(ExamAddForm examAddForm);

    List<ExamDetailRespVO> listEntityToExamDetailRespVO(List<ExamQuestion> examQuestion);

    ExamDetailVO examToExamDetailVO(Exam exam);

    ExamsubjectListVO entityToExamsubjectListVO(Exam exam);

    ExamQuestionVO examQuestionEntityToVO(ExamQuestion examQuestion);

    List<ExamQuestionVO> examQuestionListEntityToVO(List<ExamQuestion> examQuestion);

    List<OptionVO> opListEntityToVO(List<Option> examQuestion);
}
