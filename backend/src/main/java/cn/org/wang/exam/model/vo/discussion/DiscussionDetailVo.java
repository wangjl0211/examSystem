package cn.org.wang.exam.model.vo.discussion;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 讨论详情vo
 * @author Wang
 * @version 1.0
 * @since 2026/4/4 13:51
 */
@Data
public class DiscussionDetailVo {
    private Integer id;
    private String title;
    private String sender;
    private String content;
    private String subjectName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
