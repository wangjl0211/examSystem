package cn.org.wang.exam.service.exam;

import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.model.vo.record.ExamRecordDetailVO;

import java.util.List;

/**
 * 考试评分服务接口
 * 处理评分、成绩查询操作
 *
 * @author Wang
 * @version 1.0
 */
public interface ExamScoreService {

    /**
     * 获取考试记录详情
     *
     * @param examId 考试ID
     * @return 考试记录详情列表
     */
    Result<List<ExamRecordDetailVO>> details(Integer examId);
}
