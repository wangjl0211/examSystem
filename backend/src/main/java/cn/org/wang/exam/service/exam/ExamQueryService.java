package cn.org.wang.exam.service.exam;

import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.model.vo.exam.ExamDetailVO;
import cn.org.wang.exam.model.vo.exam.ExamQuestionListVO;
import cn.org.wang.exam.model.vo.exam.ExamQuCollectVO;
import cn.org.wang.exam.model.vo.exam.ExamVO;
import cn.org.wang.exam.model.vo.exam.ExamsubjectListVO;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * 考试查询服务接口
 * 处理考试的查询操作
 *
 * @author Wang
 * @version 1.0
 */
public interface ExamQueryService {

    /**
     * 分页查询考试列表
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param title 考试标题（可选）
     * @return 分页结果
     */
    Result<IPage<ExamVO>> getPagingExam(Integer pageNum, Integer pageSize, String title);

    /**
     * 获取考试详情
     *
     * @param examId 考试ID
     * @return 考试详情
     */
    Result<ExamDetailVO> getDetail(Integer examId);

    /**
     * 获取考试试题列表
     *
     * @param examId 考试ID
     * @return 试题列表
     */
    Result<ExamQuestionListVO> getQuestionList(Integer examId);

    /**
     * 获取考试题目收藏列表
     *
     * @param examId 考试ID
     * @return 收藏列表
     */
    Result<List<ExamQuCollectVO>> getCollect(Integer examId);

    /**
     * 获取课程考试列表
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param title 考试标题
     * @param isASC 是否升序
     * @return 分页结果
     */
    Result<IPage<ExamsubjectListVO>> getsubjectExamList(Integer pageNum, Integer pageSize, String title, Boolean isASC);
}
