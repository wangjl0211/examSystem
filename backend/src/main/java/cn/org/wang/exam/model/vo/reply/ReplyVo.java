package cn.org.wang.exam.model.vo.reply;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Wang
 * @version 1.0
 * @since 2026/4/4 14:29
 */
@Data
public class ReplyVo {
    private Integer id;
    private Integer userId;
    private Integer parentId;
    private Integer discussionId;
    private String avatar;
    private String content;
    private String realName;
    private Integer count;
    private Integer isLike;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    private List<ReplyVo> childReplies;
}
