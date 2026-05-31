package cn.org.wang.exam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.model.entity.UserExamsScore;
import cn.org.wang.exam.model.vo.score.SubjectScoreVO;
import cn.org.wang.exam.model.vo.score.UserScoreVO;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 成绩管理服务接口
 *
 * @author Wang
 * @since 2026-03-21
 */
public interface IUserExamsScoreService extends IService<UserExamsScore> {

    /**
     * 分页获取成绩信息
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param subjectId  课程Id
     * @param examId   考试Id
     * @param realName 真实姓名
     * @return 查询结果集
     */
    Result<IPage<UserScoreVO>> pagingScore(Integer pageNum, Integer pageSize, Integer subjectId, Integer examId, String realName);

    /**
     * 成绩导出
     *
     * @param response 响应对象
     * @param examId   考试id
     * @param subjectId  课程id
     */
    void exportScores(HttpServletResponse response, Integer examId, Integer subjectId);

    /**
     * 根据课程分析考试情况
     *
     * @param pageNum   页码
     * @param pageSize  每页大小
     * @param examTitle 考试名称
     * @return 响应结果
     */
    Result<IPage<SubjectScoreVO>> getExamScoreInfo(Integer pageNum, Integer pageSize, String examTitle, Integer subjectId);
}

