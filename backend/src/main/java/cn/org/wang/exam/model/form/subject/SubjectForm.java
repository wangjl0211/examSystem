package cn.org.wang.exam.model.form.subject;

import jakarta.validation.constraints.NotBlank;

/**
 * @Author Wang
 * @Version
 * @Date 2026/3/28 1:49 PM
 */
public class SubjectForm {
    // 课程名称
    @NotBlank
    private String subjectName;

    // 课程口令
    private String code;

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}

