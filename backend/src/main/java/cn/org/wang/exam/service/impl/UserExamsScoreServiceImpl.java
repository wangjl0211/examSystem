package cn.org.wang.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.org.wang.exam.common.exception.ServiceRuntimeException;
import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.mapper.ExamMapper;
import cn.org.wang.exam.mapper.UserExamsScoreMapper;
import cn.org.wang.exam.mapper.UserSubjectMapper;
import cn.org.wang.exam.model.entity.Exam;
import cn.org.wang.exam.model.entity.UserExamsScore;
import cn.org.wang.exam.model.vo.score.ExportScoreVO;
import cn.org.wang.exam.model.vo.score.SubjectScoreVO;
import cn.org.wang.exam.model.vo.score.UserScoreVO;
import cn.org.wang.exam.service.IUserExamsScoreService;
import cn.org.wang.exam.utils.SecurityUtil;
import cn.org.wang.exam.utils.excel.ExcelUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 成绩管理服务接口实现类
 *
 * @author Wang
 * @since 2026-03-21
 */
@Service
public class UserExamsScoreServiceImpl extends ServiceImpl<UserExamsScoreMapper, UserExamsScore> implements IUserExamsScoreService {
    @Resource
    private UserExamsScoreMapper userExamsScoreMapper;
    @Resource
    private ExamMapper examMapper;
    @Resource
    private UserSubjectMapper userSubjectMapper;

    @Override
    public Result<IPage<UserScoreVO>> pagingScore(Integer pageNum, Integer pageSize, Integer subjectId, Integer examId, String realName) {
        IPage<UserScoreVO> page = new Page<>(pageNum, pageSize);
        page = userExamsScoreMapper.pagingScore(page, subjectId, examId, realName);
        return Result.success(null, page);
    }

    @Override
    public void exportScores(HttpServletResponse response, Integer examId, Integer subjectId) {
        // 获取成绩信息
        List<ExportScoreVO> scores = userExamsScoreMapper.selectScores(examId, subjectId);
        
        // 处理空数据情况
        if (scores == null || scores.isEmpty()) {
            ExcelUtils.exportEmpty(response, "成绩导出");
            return;
        }
        
        // 设置排名
        final int[] sort = {0};
        scores.forEach(exportScoreVO -> exportScoreVO.setRanking(++sort[0]));
        
        // 获取考试名
        LambdaQueryWrapper<Exam> wrapper = new LambdaQueryWrapper<Exam>().eq(Exam::getId, examId).select(Exam::getTitle);
        Exam exam = examMapper.selectOne(wrapper);
        
        // 使用默认文件名如果考试不存在
        String fileName = exam != null ? exam.getTitle() : "成绩导出";
        
        // 生成表格并响应
        ExcelUtils.export(response, fileName, scores, ExportScoreVO.class);
    }

    @Override
    public Result<IPage<SubjectScoreVO>> getExamScoreInfo(Integer pageNum, Integer pageSize, String examTitle, Integer subjectId) {
        IPage<SubjectScoreVO> page = new Page<>(pageNum, pageSize);
        Integer userId = SecurityUtil.getUserId();
        // 根据用户id查询教师所加入的课程
        List<Integer> subjectIdList = userSubjectMapper.getSubjectIdListByUserId(userId);
        if (subjectIdList.isEmpty()) {
            throw new ServiceRuntimeException("未创建课程暂无数据");
        }
        Integer roleCode = SecurityUtil.getRoleCode();
        page = userExamsScoreMapper.scoreStatistics(page, subjectId, examTitle, userId, roleCode, subjectIdList);
        return Result.success("查询成功", page);
    }
}

