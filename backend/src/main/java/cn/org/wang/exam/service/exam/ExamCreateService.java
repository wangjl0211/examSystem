package cn.org.wang.exam.service.exam;

import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.model.form.exam.ExamAddForm;
import cn.org.wang.exam.model.form.exam.ExamUpdateForm;

/**
 * 考试创建服务接口
 * 处理考试的创建、编辑、删除操作
 *
 * @author Wang
 * @version 1.0
 */
public interface ExamCreateService {

    /**
     * 创建考试
     *
     * @param examAddForm 考试表单
     * @return 操作结果
     */
    Result<String> createExam(ExamAddForm examAddForm);

    /**
     * 更新考试
     *
     * @param examUpdateForm 考试更新表单
     * @param examId 考试ID
     * @return 操作结果
     */
    Result<String> updateExam(ExamUpdateForm examUpdateForm, Integer examId);

    /**
     * 删除考试
     *
     * @param ids 考试ID列表（逗号分隔）
     * @return 操作结果
     */
    Result<String> deleteExam(String ids);
}
