package cn.org.wang.exam.model.vo.subject;


import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author Wang
 * @Version
 * @Date 2026/3/28 8:06 PM
 */
@Data
public class SubjectVO {

    /**
     * id   课程表
     */
    private Integer id;

    /**
     * 课程名称
     */
    private String subjectName;

    /**
     * 创建人id
     */
    private Integer userId;

    /**
     * 创建人名称
     */
    private String userName;

    /**
     * 课程口令
     */
    private String code;

    /**
     * 课程人数
     */
    private Integer subjectCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 加入时间
     */
    private LocalDateTime joinTime;
}
