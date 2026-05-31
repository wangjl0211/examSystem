package cn.org.wang.exam.model.enums;

/**
 * 交卷来源枚举
 */
public enum ExamSubmitSource {
    /** 用户主动交卷 */
    USER,
    /** 考试时间到自动交卷 */
    TIMEOUT,
    /** 切屏超限等强制交卷 */
    FORCE
}
