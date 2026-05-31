package cn.org.wang.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import cn.org.wang.exam.model.entity.NoticeSubject;

/**
 * 公告课程关联Mapper接口
 *
 * @author Wang
 * @since 2026-03-21
 */
public interface NoticeSubjectMapper extends BaseMapper<NoticeSubject> {

    /**
     * 添加公告和课程的关联
     *
     * @param noticeId    公告ID
     * @param subjectIdList 课程ID列表
     * @return 影响行数
     */
    int addNoticesubject(Integer noticeId, java.util.List<Integer> subjectIdList);

    /**
     * 删除公告和课程的关联
     *
     * @param noticeIds 公告ID列表
     * @return 影响行数
     */
    int deleteNoticesubject(java.util.List<Integer> noticeIds);

    /**
     * 删除公告和课程的关联
     *
     * @param noticeId 公告ID
     * @return 影响行数
     */
    int delNoticesubject(Integer noticeId);

    /**
     * 获取公告关联的课程列表
     *
     * @param noticeId 公告ID
     * @return 课程ID列表
     */
    java.util.List<Integer> getsubjectList(Integer noticeId);

    /**
     * 获取课程关联的公告ID列表
     *
     * @param subjectId 课程ID
     * @return 公告ID列表
     */
    java.util.List<Integer> getNoticeIdList(Integer subjectId);

}