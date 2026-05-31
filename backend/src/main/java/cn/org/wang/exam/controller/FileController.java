package cn.org.wang.exam.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.service.IFileService;

/**
 * 说明：
 * 文件处理Controller
 *
 * @Author Wang
 * @Version 1.0
 * @Date 2026/4/6 3:23 PM
 */
@RestController
@Tag(name = "文件服务接口")
@RequestMapping("/api/upload")
public class FileController {
    private final IFileService fileService;

    // 构造器注入
    public FileController(IFileService fileService) {
        this.fileService = fileService;
    }
    /**
     * 上传图片
     *
     * @param file 文件
     * @return 返回头像地址
     */
    @Operation(summary ="上传图片")
    @PostMapping("/image")
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<String> uploadAvatar(@RequestPart("file") MultipartFile file) {
        return fileService.uploadImage(file);
    }
}

