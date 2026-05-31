package cn.org.wang.exam.constants;

/**
 * 系统常量定义
 * 定义系统中使用的常量值，避免魔法值硬编码
 *
 * @author Wang
 * @version 1.0
 */
public final class SystemConstants {

    private SystemConstants() {
        // 防止实例化
    }

    /**
     * 角色ID常量
     */
    public static final int ROLE_ADMIN = 0;      // 管理员
    public static final int ROLE_TEACHER = 1;    // 教师
    public static final int ROLE_STUDENT = 2;    // 学生

    /**
     * 用户状态常量
     */
    public static final int STATUS_DISABLED = 0; // 禁用
    public static final int STATUS_ENABLED = 1;  // 启用

    /**
     * 删除标记常量
     */
    public static final int NOT_DELETED = 0;     // 未删除
    public static final int DELETED = 1;         // 已删除

    /**
     * 身份类型常量
     */
    public static final String IDENTITY_TEACHER = "teacher";
    public static final String IDENTITY_STUDENT = "student";

    /**
     * 答案正确状态
     */
    public static final int ANSWER_CORRECT = 1;   // 正确
    public static final int ANSWER_INCORRECT = 0; // 错误
    public static final int ANSWER_UNGRADED = -1; // 未批改

    /**
     * 考试状态
     */
    public static final int EXAM_IN_PROGRESS = 0; // 进行中
    public static final int EXAM_COMPLETED = 1;   // 已完成
}
