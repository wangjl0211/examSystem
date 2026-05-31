package cn.org.wang.exam.model.enums;

/**
 * 说明：
 * 试题类型枚举管理
 *
 * @Author Wang
 * @Version 1.0
 * @Date 2026/4/26 3:12 PM
 */
public enum QuestionType {
    SINGLE_CHOICE_QUESTIONS(1,"单选题"),
    MULTIPLE_CHOICE_QUESTIONS(2,"多选题"),
    TRUE_OR_FALSE_QUESTIONS(3,"判断题"),
    SHORT_ANSWER_QUESTIONS(4,"简答题"),
    // 其他状态...
    ;
    private final int code;
    private final String desc;

    QuestionType(int code, String desc) {
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
