package cn.org.wang.exam.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 公告课程关联实体类
 *
 * @author Wang
 * @since 2026-03-21
 */
@Data
@Schema(description ="公告课程关联实体类")
@TableName("t_notice_subject")
public class NoticeSubject implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description ="公告课程关联表ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description ="公告ID")
    private Integer noticeId;

    @Schema(description ="课程ID")
    private Integer subjectId;

    @TableLogic
    @Schema(description ="逻辑删除字段")
    private Integer isDeleted;
}

