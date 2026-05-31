package cn.org.wang.exam.model.vo.question;

import com.fasterxml.jackson.annotation.JsonFormat;

import cn.org.wang.exam.model.entity.Option;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Wang
 * @Version 1.0
 * @Date 2026/4/2 20:27
 */
@Data
public class QuestionVO {
    private Integer id;
    // 题干
    private String content;
    // 题库ID
    private Integer repoId;
    // 图片
    private  String image;
    // 题库标题
    private String repoTitle;
    // 试题类型
    private Integer quType;
    private String analysis;
    // 创建试卷
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    // 选项列表
    private List<Option> options;
}
