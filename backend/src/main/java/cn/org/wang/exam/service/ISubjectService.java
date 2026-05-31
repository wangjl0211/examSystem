package cn.org.wang.exam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.model.entity.Subject;
import cn.org.wang.exam.model.form.subject.SubjectForm;
import cn.org.wang.exam.model.vo.subject.SubjectVO;

import java.util.List;
import java.util.Map;

/**
 * 课程服务类
 *
 * @Author Wang
 * @since 2026-03-21
 */
public interface ISubjectService extends IService<Subject> {

    /**
     * 添加课程
     *
     * @param subjectForm
     * @return
     */
    Result<String> addSubject(SubjectForm subjectForm);

    /**
     * 修改课程
     *
     * @param id        课程ID
     * @param subjectForm
     * @return
     */
    Result<String> updateSubject(Integer id, SubjectForm subjectForm);

    /**
     * 删除课程
     *
     * @param id 课程ID
     * @return
     */
    Result<String> deleteSubject(Integer id);

    /**
     * 分页查找课程
     *
     * @param pageNum   页码
     * @param pageSize  每页大小
     * @param subjectName 课程名称
     * @param userName 创建用户姓名
     * @param createDate 创建日期
     * @return
     */
    Result<IPage<SubjectVO>> getPaging(Integer pageNum, Integer pageSize, String subjectName, String userName, String createDate);

    /**
     * 移除课程
     *
     * @param ids 课程代码
     * @return
     */
    Result<String> removeUserSubject(String ids);

    /**
     * 获取所有课程列表
     *
     * @return
     */
    Result<List<SubjectVO>> getAllSubject();

    /**
     * 学生加入课程
     *
     * @param code
     * @return
     */
    Result<String> joinSubject(String code);

    /**
     * 学生退出课程
     *
     * @param subjectId
     * @return
     */
    Result<String> exitSubject(String subjectId);

    /**
     * 学生退出课程
     *
     * @return
     */
    Result<String> userExitSubject();

    /**
     * 获取课程详情
     *
     * @param subjectId 课程ID
     * @return
     */
    Result<Map<String, Object>> getSubjectDetail(Integer subjectId);

    /**
     * 从课程中移除用户
     *
     * @param subjectId 课程ID
     * @param userId 用户ID
     * @return
     */
    Result<String> removeUserFromSubject(Integer subjectId, Integer userId);
}



