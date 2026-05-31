package cn.org.wang.exam.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.model.entity.Reply;
import cn.org.wang.exam.model.form.reply.ReplyForm;
import cn.org.wang.exam.model.vo.reply.ReplyVo;
import cn.org.wang.exam.service.IReplyService;

import java.util.List;

/**
 * @author Wang
 * @version 1.0
 * @since 2026/4/4 15:18
 */
@RestController
@Tag(name = "回复相关接口")
@RequestMapping("/api/reply")
public class ReplyController {

    @Resource
    private IReplyService replyService;

    /**
     * 新增回复
     *
     * @param replyForm 入参
     * @return 新增后的回复
     */
    @PostMapping("add")
    @Operation(summary = "添加回复")
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher')")
    public Result<Reply> addReply(@RequestBody ReplyForm replyForm) {
        Reply reply = replyService.addReply(replyForm);
        return Result.success("回复成功", reply);
    }

    /**
     * 根据id删除回复
     *
     * @param id id
     * @return 返回id
     */
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    @Operation(summary = "删除回复")
    @DeleteMapping("/delete/{id}")
    public Result<Integer> deleteReply(@PathVariable("id") Integer id) {
        Integer delId = replyService.deleteReply(id);
        return Result.success("删除成功", delId);
    }

    /**
     * 根据讨论id获取评论
     *
     * @param orderBy 排序方式 1时间升序 2时间降序 3点赞数量升序 4点赞数量降序
     * @param id      讨论id
     * @return 评论
     */
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher')")
    @Operation(summary = "根据讨论id获取评论")
    @GetMapping("/query/{orderBy}/{id}")
    public Result<List<ReplyVo>> queryReplyByDiscussionId(@PathVariable("orderBy") Integer orderBy, @PathVariable("id") Integer id) {
        List<ReplyVo> replyVos = replyService.queryReplyByDiscussionId(orderBy, id);
        return Result.success("查询成功", replyVos);
    }

}
