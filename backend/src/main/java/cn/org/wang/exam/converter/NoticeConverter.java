package cn.org.wang.exam.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.org.wang.exam.config.MapStructConfig;
import cn.org.wang.exam.model.entity.Notice;
import cn.org.wang.exam.model.form.notice.NoticeForm;
import cn.org.wang.exam.model.vo.notice.NoticeVO;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

/**
 * @Author Wang
 * @Version
 * @Date 2026/3/28 11:04 PM
 */
@Component
@Mapper(componentModel="spring", config = MapStructConfig.class)
public interface NoticeConverter {

    Notice formToEntity(NoticeForm noticeForm);

    Page<NoticeVO> pageEntityToVo(Page<Notice> noticePage);

    NoticeVO noticeToNoticeVO(Notice notice);
}
