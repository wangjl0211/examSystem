package cn.org.wang.exam.model.vo.subject;

import java.time.LocalDateTime;

/**
 * 课程用户VO
 *
 * @Author Wang
 * @since 2026-03-21
 */
public class SubjectUserVO {
    private Integer userId;
    private String userNo;
    private String realName;
    private LocalDateTime joinTime;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserNo() {
        return userNo;
    }

    public void setUserNo(String userNo) {
        this.userNo = userNo;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public LocalDateTime getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(LocalDateTime joinTime) {
        this.joinTime = joinTime;
    }
}
