package cn.org.wang.exam.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;

import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.model.form.subject.SubjectForm;
import cn.org.wang.exam.model.vo.subject.SubjectVO;
import cn.org.wang.exam.service.ISubjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 课程管理
 *
 * @Author Wang
 * @since 2026-03-21
 */
@Tag(name = "课程管理相关接口")
@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    @Resource
    private ISubjectService subjectService;

    /**
     * 分页查询课程
     *
     * @param pageNum   页码
     * @param pageSize  每页大小
     * @param subjectName 课程名称
     * @param userName 创建用户姓名
     * @param createDate 创建日期
     * @return
     */
    @Operation(summary ="分页查询课程")
    @GetMapping("/paging")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<IPage<SubjectVO>> getsubject(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                           @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize,
                                           @RequestParam(value = "subjectName", required = false) String subjectName,
                                           @RequestParam(value = "userName", required = false) String userName,
                                           @RequestParam(value = "createDate", required = false) String createDate) {
        return subjectService.getPaging(pageNum, pageSize, subjectName, userName, createDate);
    }

    /**
     * 创建课程
     *
     * @param subjectForm
     * @return
     */
    @Operation(summary ="创建课程")
    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('role_admin','role_teacher')")
    public Result<String> addSubject(@Validated @RequestBody SubjectForm subjectForm) {
        return subjectService.addSubject(subjectForm);
    }

    /**
     * 修改课程
     *
     * @param id
     * @param subjectForm
     * @return
     */
    @Operation(summary ="修改课程")
    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyAuthority('role_admin','role_teacher')")
    public Result<String> updateSubject(@PathVariable("id") @NotNull Integer id, @Validated @RequestBody SubjectForm subjectForm) {
        return subjectService.updateSubject(id, subjectForm);
    }

    /**
     * 删除课程
     *
     * @param id 课程ID
     * @return
     */
    @Operation(summary ="删除课程")
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('role_admin','role_teacher')")
    public Result<String> deleteSubject(@PathVariable("id") @NotNull Integer id) {
        return subjectService.deleteSubject(id);
    }

    /**
     * 退出课程
     *
     * @param ids 课程ID
     * @return
     */
    @Operation(summary ="退出课程")
    @PatchMapping("/remove/{ids}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<String> removeUserSubject(@PathVariable("ids") @NotBlank String ids) {
        return subjectService.removeUserSubject(ids);
    }

    /**
     * 获取所有课程列表
     *
     * @return
     */
    @Operation(summary ="获取所有课程列表")
    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<List<SubjectVO>> getAllSubject() {
        return subjectService.getAllSubject();
    }

    /**
     * 学生加入课程
     *
     * @param code 课程代码
     * @return
     */
    @Operation(summary ="学生加入课程")
    @GetMapping("/student/join")
    @PreAuthorize("hasAnyAuthority('role_student')")
    public Result<String> joinSubject(@RequestParam("code") String code) {
        return subjectService.joinSubject(code);
    }

    /**
     * 学生退出课程
     *
     * @param subjectId 课程ID
     * @return
     */
    @Operation(summary ="学生退出课程")
    @DeleteMapping("/student/exit/{subjectId}")
    @PreAuthorize("hasAnyAuthority('role_student')")
    public Result<String> exitSubject(@PathVariable("subjectId") Integer subjectId) {
        return subjectService.exitSubject(subjectId.toString());
    }

    /**
     * 学生退出课程
     *
     * @return
     */
    @Operation(summary ="学生退出课程")
    @PutMapping("/student/leave-primary-subject")
    @PreAuthorize("hasAnyAuthority('role_student')")
    public Result<String> userExitSubject() {
        return subjectService.userExitSubject();
    }

    /**
     * 获取课程详情
     *
     * @param subjectId 课程ID
     * @return
     */
    @Operation(summary ="获取课程详情")
    @GetMapping("/detail/{subjectId}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<Map<String, Object>> getSubjectDetail(@PathVariable("subjectId") Integer subjectId) {
        return subjectService.getSubjectDetail(subjectId);
    }

    /**
     * 从课程中移除用户
     *
     * @param subjectId 课程ID
     * @param userId 用户ID
     * @return
     */
    @Operation(summary ="从课程中移除用户")
    @DeleteMapping("/remove-user/{subjectId}/{userId}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> removeUserFromSubject(@PathVariable("subjectId") Integer subjectId, @PathVariable("userId") Integer userId) {
        return subjectService.removeUserFromSubject(subjectId, userId);
    }
}

