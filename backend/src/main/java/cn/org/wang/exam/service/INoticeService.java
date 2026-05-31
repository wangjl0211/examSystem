package cn.org.wang.exam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.model.entity.Notice;
import cn.org.wang.exam.model.form.notice.NoticeForm;
import cn.org.wang.exam.model.vo.notice.NoticeVO;

/**
 * 公告服务接口
 *
 * @Author Wang
 * @since 2026-03-21
 */
public interface INoticeService extends IService<Notice> {

    /**
     * 添加公告
     *
     * @param noticeForm
     * @return
     */
    Result<String> addNotice(NoticeForm noticeForm);

    /**
     * 删除公告
     *
     * @param ids 删除公告ID
     * @return
     */
    Result<String> deleteNotice(String ids);

    /**
     * 修改公告
     *
     * @param id 公告ID
     * @param noticeForm
     * @return
     */
    Result<String> updateNotice(Integer id, NoticeForm noticeForm);

    /**
     * 教师分页查找
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param title
     * @return
     */
    Result<IPage<NoticeVO>> getNotice(Integer pageNum, Integer pageSize, String title);

    /**
     * 查找最新公告-用户
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return
     */
    Result<IPage<NoticeVO>> getNewNotice(Integer pageNum, Integer pageSize);
}
