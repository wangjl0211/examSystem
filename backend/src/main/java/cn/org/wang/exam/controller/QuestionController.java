package cn.org.wang.exam.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;

import cn.org.wang.exam.common.group.QuestionGroup;
import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.model.form.question.QuestionFrom;
import cn.org.wang.exam.model.vo.question.QuestionVO;
import cn.org.wang.exam.service.IFileService;
import cn.org.wang.exam.service.IQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 试题管理
 *
 * @author Wang
 * @since 2026-03-21
 */
@Tag(name = "试题管理相关接口")
@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    @Resource
    private IQuestionService iQuestionService;

    @Resource
    private IFileService fileService;

    /**
     * 单题添加
     *
     * @param questionFrom 传参
     * @return 响应
     */
    @Operation(summary = "单题添加")
    @PostMapping("/single")
    @PreAuthorize("hasAuthority('role_teacher')")
    public Result<String> addSingleQuestion(@Validated(QuestionGroup.QuestionAddGroup.class) @RequestBody QuestionFrom questionFrom) {
        return iQuestionService.addSingleQuestion(questionFrom);
    }

    /**
     * 批量删除试题
     *
     * @param ids 试题id
     * @return 相应
     */
    @Operation(summary = "批量删除试题")
    @DeleteMapping("/batch/{ids}")
    @PreAuthorize("hasAuthority('role_teacher')")
    public Result<String> deleteBatchQuestion(@PathVariable("ids") String ids) {
        return iQuestionService.deleteBatchByIds(ids);
    }

    /**
     * 分页查询试题
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param content  试题名
     * @param repoId   题库id
     * @param type     试题类型
     * @return 响应
     */
    @Operation(summary = "分页查询试题")
    @GetMapping("/paging")
    @PreAuthorize("hasAuthority('role_teacher')")
    public Result<IPage<QuestionVO>> pagingQuestion(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                                    @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                                    @RequestParam(value = "content", required = false) String content,
                                                    @RequestParam(value = "repoId", required = false) Integer repoId,
                                                    @RequestParam(value = "type", required = false) Integer type) {
        return iQuestionService.pagingQuestion(pageNum, pageSize, content, type, repoId);
    }

    /**
     * 根据试题id获取单题详情
     *
     * @param id 试题id
     * @return 响应结果
     */
    @Operation(summary = "根据试题id获取单题详情")
    @GetMapping("/single/{id}")
    @PreAuthorize("hasAuthority('role_teacher')")
    public Result<QuestionVO> querySingle(@PathVariable("id") Integer id) {
        return iQuestionService.querySingle(id);
    }

    /**
     * 修改试题
     *
     * @param id           试题Id
     * @param questionFrom 入参
     * @return 响应结果
     */
    @Operation(summary = "修改试题")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('role_teacher')")
    public Result<String> updateQuestion(@PathVariable("id") Integer id, @RequestBody QuestionFrom questionFrom) {
        questionFrom.setId(id);
        return iQuestionService.updateQuestion(questionFrom);
    }

    /**
     * 批量导入试题
     *
     * @param id   题库Id
     * @param file Excel文件
     * @return 响应结果
     */
    @Operation(summary = "批量导入试题")
    @PostMapping("/import/{id}")
    @PreAuthorize("hasAuthority('role_teacher')")
    public Result<String> importQuestion(@PathVariable("id") Integer id, @RequestParam("file") MultipartFile file) {
        return iQuestionService.importQuestion(id, file);
    }

    /**
     * 上传试题图片
     *
     * @param file 文件
     * @return 返回上传后的地址
     */
    @Operation(summary = "上传图片")
    @PostMapping("/uploadImage")
    @PreAuthorize("hasAuthority('role_teacher')")
    public Result<String> uploadImage(@RequestPart("file") MultipartFile file) {
        return fileService.uploadImage(file);
    }
}
