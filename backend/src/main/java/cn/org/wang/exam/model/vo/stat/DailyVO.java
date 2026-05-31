package cn.org.wang.exam.model.vo.stat;

import lombok.Data;

import java.time.LocalDate;

/**
 * @Author Wang
 * @Version
 * @Date 2026/5/29 8:10 AM
 */
@Data
public class DailyVO {
    private Integer id;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 登录日期
     */
    private LocalDate loginDate;

    /**
     * 累积在线秒数
     */
    private Integer totalSeconds;
}
