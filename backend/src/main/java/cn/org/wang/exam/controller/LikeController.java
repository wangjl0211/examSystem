package cn.org.wang.exam.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.model.form.like.LikeForm;
import cn.org.wang.exam.service.ILikeService;

/**
 * @author Wang
 * @version 1.0
 * @since 2026/4/16 22:20
 */
@RestController
@RequestMapping("/api/like")
@Tag(name = "点赞相关api")
@PreAuthorize("isAuthenticated()")  // 修复P0安全漏洞：所有接口需要认证
public class LikeController {

    @Resource
    private ILikeService likeService;

    @PostMapping("/doLike")
    @Operation(summary ="点赞或取消点赞")
    public Result<String> doLike(@Validated @RequestBody LikeForm likeForm) {
        return likeService.doLike(likeForm);
    }
}

