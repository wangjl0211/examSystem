package cn.org.wang.exam.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.org.wang.exam.common.exception.ServiceRuntimeException;
import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.converter.NoticeConverter;
import cn.org.wang.exam.mapper.NoticeMapper;
import cn.org.wang.exam.mapper.NoticeSubjectMapper;
import cn.org.wang.exam.mapper.UserMapper;
import cn.org.wang.exam.mapper.UserSubjectMapper;
import cn.org.wang.exam.model.entity.Notice;
import cn.org.wang.exam.model.form.notice.NoticeForm;
import cn.org.wang.exam.model.vo.notice.NoticeVO;
import cn.org.wang.exam.service.INoticeService;
import cn.org.wang.exam.utils.SecurityUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 公告服务实现类
 *
 * @Author Wang
 * @since 2026-03-21
 */
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements INoticeService {
    @Resource
    private NoticeMapper noticeMapper;
    @Resource
    private NoticeConverter noticeConverter;
    @Resource
    private NoticeSubjectMapper noticeSubjectMapper;
    @Resource
    private UserSubjectMapper userSubjectMapper;
    @Resource
    private UserMapper userMapper;

    /**
     * 新建公告
     * @param noticeForm
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> addNotice(NoticeForm noticeForm) {
        String subjectIds = noticeForm.getsubjectIds();
        // 设置创建人
        noticeForm.setUserId(SecurityUtil.getUserId());
        // 添加公告
        Notice notice = noticeConverter.formToEntity(noticeForm);
        // 管理员全部公开
        Integer roleCode = SecurityUtil.getRoleCode();
        if(roleCode==3){
            notice.setIsPublic(1);
        }
        // 判断是否不公开公告
        if(noticeForm.getIsPublic()==0){
            if(subjectIds==null|| "".equals(subjectIds)){
                throw new ServiceRuntimeException("公开课程必须添入课程");
            }
            int addNoticeRowOther = noticeMapper.insert(notice);
            if (addNoticeRowOther == 0) {
                throw new ServiceRuntimeException("添加公告失败");
            }
            //添加公告和课程对应关系
            Integer noticeId = notice.getId();
            List<Integer> subjectIdList = Arrays.stream(subjectIds.split(","))
                    .map(Integer::parseInt)
                    .toList();
            int addNoticesubjectRow = noticeSubjectMapper.addNoticesubject(noticeId,subjectIdList);
            if (addNoticesubjectRow == 0) {
                throw new ServiceRuntimeException("添加公告条数=0失败");
            }
        }else{
            int addNoticeRowAdmin = noticeMapper.insert(notice);
            if (addNoticeRowAdmin == 0) {
                throw new ServiceRuntimeException("添加公告失败");
            }
        }
        return Result.success("添加公告成功");
    }

    /**
     * 删除公告
     * @param ids
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> deleteNotice(String ids) {
        // 转换为集合
        List<Integer> noticeIds = Arrays.stream(ids.split(","))
                .map(Integer::parseInt)
                .toList();
        // 删除公告
        noticeMapper.deleteBatchIds(noticeIds);
        noticeSubjectMapper.deleteNoticesubject(noticeIds);
        return Result.success("删除成功");
    }

    /**
     * 更新公告
     * @param noticeId
     * @param noticeForm
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateNotice(Integer noticeId, NoticeForm noticeForm) {
        Integer isPublic =  noticeMapper.getIsPublic(noticeId);
        String subjectIds = noticeForm.getsubjectIds();
        List<Integer> subjectIdList = null;
        if(subjectIds!=null&&!subjectIds.isEmpty()){
            subjectIdList = Arrays.stream(subjectIds.split(","))
                    .map(Integer::parseInt)
                    .toList();
        }
        if(!isPublic.equals(noticeForm.getIsPublic())&&isPublic==0){
            // 从不公开改成公开，就是修改公告内容改变公开状态，删除公告课程对应关系
            noticeMapper.updateNotice(noticeId,noticeForm);
            noticeSubjectMapper.delNoticesubject(noticeId);
        }else if(!isPublic.equals(noticeForm.getIsPublic())&&isPublic==1){
            // 从公开改成不公开和状态不变，就是修改公告内容改变公开状态，删除原有的关系添加新公告课程对应关系
            noticeMapper.updateNotice(noticeId,noticeForm);
            noticeSubjectMapper.addNoticesubject(noticeId,subjectIdList);
        }
        // 公开修改公开，不公开修改不公开
        if(isPublic.equals(noticeForm.getIsPublic())&&isPublic==1){
            // 如果是公开的
            // 直接修改内容
            noticeMapper.updateNotice(noticeId,noticeForm);
        }else if (isPublic.equals(noticeForm.getIsPublic())&&isPublic==0){
            // 如果是不同开的 可以修改内容和课程关系
            noticeMapper.updateNotice(noticeId,noticeForm);
            noticeSubjectMapper.delNoticesubject(noticeId);
            noticeSubjectMapper.addNoticesubject(noticeId,subjectIdList);
        }
        return Result.success("修改成功");
    }

    /**
     * 教师管理员获取公告
     * @param pageNum
     * @param pageSize
     * @param title
     * @return
     */
    @Override
    public Result<IPage<NoticeVO>> getNotice(Integer pageNum, Integer pageSize, String title) {
        IPage<NoticeVO> page = new Page<>(pageNum, pageSize);
        Integer userId = SecurityUtil.getUserId();
        // 分页查找公告
        List<NoticeVO> records = noticeMapper.getNotice(userId,title);
        // 设置课程列表
        for(NoticeVO temp: records){
            List<Integer> subjectList = noticeSubjectMapper.getsubjectList(temp.getId());
            temp.setsubjectIds(subjectList);
        }
        page.setRecords(records);
        return Result.success("查询成功", page);

    }

    /**
     * 学生获取公告
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public Result<IPage<NoticeVO>> getNewNotice(Integer pageNum, Integer pageSize) {
        // 创建分页对象
        Page<NoticeVO> page = new Page<>(pageNum, pageSize);
        // 获取课程的所有教师列表
        Integer subjectId = SecurityUtil.getSubjectId();
        // 教师列表
        List<Integer> teachIdList= userSubjectMapper.getUserListBysubjectId(subjectId);
        // 获取课程和公告关联的公告idlist
        List<Integer> noticeIdList = noticeSubjectMapper.getNoticeIdList(subjectId);
        // 查找这些教师非公开的和这个课程的关联的公告，和这些教师和管理员公开的公告
        List<Integer> adminIdList = userMapper.getAdminList();
        // 查找公告
        page = noticeMapper.getNewNotice(page,teachIdList,noticeIdList,adminIdList);
        return Result.success("查询成功", page);
    }
}

