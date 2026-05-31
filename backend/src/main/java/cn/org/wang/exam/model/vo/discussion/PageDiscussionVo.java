package cn.org.wang.exam.model.vo.discussion;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Wang
 * @version 1.0
 * @since 2026/4/4 13:31
 */
@Data
public class PageDiscussionVo {
    private Integer id;
    private String title;
    private String sender;
    private String subjectName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
