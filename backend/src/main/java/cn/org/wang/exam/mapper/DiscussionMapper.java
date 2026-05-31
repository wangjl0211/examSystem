package cn.org.wang.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.org.wang.exam.model.entity.Discussion;
import cn.org.wang.exam.model.vo.discussion.DiscussionDetailVo;
import cn.org.wang.exam.model.vo.discussion.PageDiscussionVo;

import org.apache.ibatis.annotations.Mapper;

/**
 * @author Wang
 * @version 1.0
 * @since 2026/4/3 9:43
 */
@Mapper
public interface DiscussionMapper extends BaseMapper<Discussion> {
    /**
     * 教师分页获取自己的发布的讨论
     *
     * @param page    分页信息
     * @param userId  教师id
     * @param title   讨论标题
     * @param subjectId 课程id
     * @return 分页查询结果
     */
    Page<PageDiscussionVo> selectOwnerPage(Page<PageDiscussionVo> page, Integer userId, String title, Integer subjectId);

    /**
     * 获取讨论详情
     *
     * @param id 讨论id
     * @return 讨论信息
     */
    DiscussionDetailVo selectDetail(Integer id);

    /**
     * 学生根据课程id分页查询讨论信息
     *
     * @param page    分页信息
     * @param title   标题
     * @param subjectId 课程id
     * @return 分页查询结果
     */
    Page<PageDiscussionVo> selectDiscussionBysubjectPage(Page<PageDiscussionVo> page, String title, Integer subjectId);

    /**
     * 获取所有讨论信息（不分课程）
     *
     * @param page    分页信息
     * @param title   标题
     * @return 分页查询结果
     */
    Page<PageDiscussionVo> selectAllDiscussionPage(Page<PageDiscussionVo> page, String title);

    /**
     * 学生获取已加入课程的讨论信息
     *
     * @param page    分页信息
     * @param userId  用户ID
     * @param title   标题
     * @return 分页查询结果
     */
    Page<PageDiscussionVo> selectStudentDiscussionPage(Page<PageDiscussionVo> page, Integer userId, String title);

    /**
     * 根据课程ID删除讨论
     *
     * @param subjectId 课程ID
     * @return 删除数量
     */
    int deleteBySubjectId(Integer subjectId);
}
