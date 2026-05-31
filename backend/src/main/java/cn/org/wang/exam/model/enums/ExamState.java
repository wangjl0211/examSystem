package cn.org.wang.exam.model.enums;

/**
 * 考试状态
 */
public enum ExamState {
    ONGOING(0, "考试中"),
    SUBMITTED(1, "已交卷"),
    // 其他状态...
    ;
    
    private final int code;
    private final String desc;

    ExamState(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}