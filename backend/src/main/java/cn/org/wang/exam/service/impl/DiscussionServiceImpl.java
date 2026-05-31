package cn.org.wang.exam.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.org.wang.exam.common.exception.ServiceRuntimeException;
import cn.org.wang.exam.converter.DiscussionConverter;
import cn.org.wang.exam.mapper.DiscussionMapper;
import cn.org.wang.exam.mapper.ReplyMapper;
import cn.org.wang.exam.model.entity.Discussion;
import cn.org.wang.exam.model.form.discussion.DiscussionForm;
import cn.org.wang.exam.model.vo.discussion.DiscussionDetailVo;
import cn.org.wang.exam.model.vo.discussion.PageDiscussionVo;
import cn.org.wang.exam.service.IDiscussionService;
import cn.org.wang.exam.utils.SecurityUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Wang
 * @version 1.0
 * @since 2026/4/3 9:41
 */
@Service
public class DiscussionServiceImpl extends ServiceImpl<DiscussionMapper, Discussion> implements IDiscussionService {

    @Resource
    private DiscussionConverter discussionConverter;
    @Resource
    private ReplyMapper replyMapper;

    @Override
    public Discussion createDiscussion(DiscussionForm discussionForm) {
        // 获取当前用户id
        Integer userId = SecurityUtil.getUserId();
        // 创建讨论实体
        Discussion discussion = new Discussion();
        discussion.setTitle(discussionForm.getTitle());
        discussion.setContent(discussionForm.getContent());
        discussion.setSubjectId(discussionForm.getSubjectId());
        discussion.setUserId(userId);
        int inserted = baseMapper.insert(discussion);
        if (inserted <= 0) {
            throw new ServiceRuntimeException("创建讨论失败");
        }
        return discussion;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteDiscussion(Integer id) {
        // 先做一个非空判断
        Discussion discussion = baseMapper.selectById(id);
        if (discussion == null) {
            throw new ServiceRuntimeException("该id无对应的讨论");
        }
        // 删除讨论
        int deleted = baseMapper.deleteById(id);
        // 删除该讨论的所有回复

        if (deleted > 0) {
            // 删除成功返回id
            return id;
        }
        throw new ServiceRuntimeException("删除讨论失败");

    }

    @Override
    public Page<PageDiscussionVo> getOwnerDiscussions(String title, Integer subjectId, Integer currentPage, Integer size) {
        Page<PageDiscussionVo> page = new Page<>(currentPage, size);
        return baseMapper.selectOwnerPage(page, SecurityUtil.getUserId(), title, subjectId);
    }


    @Override
    public DiscussionDetailVo getDiscussionDetail(Integer id) {
        return baseMapper.selectDetail(id);
    }

    @Override
    public Page<PageDiscussionVo> pageDiscussionBysubject(String title, Integer currentPage, Integer size) {
        Page<PageDiscussionVo> page = new Page<>(currentPage, size);
        // 学生只能看到自己已加入课程的讨论
        return baseMapper.selectStudentDiscussionPage(page, SecurityUtil.getUserId(), title);
    }


}

