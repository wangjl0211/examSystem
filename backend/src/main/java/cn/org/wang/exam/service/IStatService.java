package cn.org.wang.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;

import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.model.entity.Examsubject;
import cn.org.wang.exam.model.vo.stat.AllStatsVO;
import cn.org.wang.exam.model.vo.stat.DailyVO;
import cn.org.wang.exam.model.vo.stat.SubjectExamVO;
import cn.org.wang.exam.model.vo.stat.SubjectStudentVO;

import java.util.List;
import java.util.Map;

/**
 * 统计管理服务接口
 *
 * @author Wang
 * @since 2026-03-21
 */
public interface IStatService extends IService<Examsubject> {

    /**
     * 各班人数统计
     *
     * @return 响应结果
     */
    Result<List<SubjectStudentVO>> getStudentSubjectCount();

    /**
     * 各课程的试卷数统计
     *
     * @return 响应结果
     */
    Result<List<SubjectExamVO>> getExamSubjectCount();

    /**
     * 统计所有课程、试卷、试题数量
     *
     * @return 响应结果
     */
    Result<AllStatsVO> getAllCount();

    /**
     * 获取用户登录时间统计
     *
     * @return
     */
    Result<List<DailyVO>> getDaily();

    /**
     * 获取管理员主页数据
     *
     * @return
     */
    Result<Map<String, Object>> getDashboard();
}
