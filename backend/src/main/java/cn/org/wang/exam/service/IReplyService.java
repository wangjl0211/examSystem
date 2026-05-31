package cn.org.wang.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;

import cn.org.wang.exam.model.entity.Reply;
import cn.org.wang.exam.model.form.reply.ReplyForm;
import cn.org.wang.exam.model.vo.reply.ReplyVo;

import java.util.List;

/**
 * @author Wang
 * @version 1.0
 * @since 2026/4/4 14:05
 */
public interface IReplyService extends IService<Reply> {

    /**
     * 新增回复
     *
     * @param replyForm 入参
     * @return 新增后的回复
     */
    Reply addReply(ReplyForm replyForm);

    /**
     * 根据id删除回复
     *
     * @param id id
     * @return 返回id
     */
    Integer deleteReply(Integer id);

    /**
     * 根据讨论id获取评论
     *
     * @param orderBy      排序方式 1时间升序 2时间降序 3点赞数量升序 4点赞数量降序
     * @param discussionId 讨论id
     * @return 评论
     */
    List<ReplyVo> queryReplyByDiscussionId(Integer orderBy, Integer discussionId);

    /**
     * 根据讨论id删除回复
     * @param discussionId  讨论id
     * @return 影响数据库记录条数
     */
    int deleteByDiscussionId(Integer discussionId);

}
