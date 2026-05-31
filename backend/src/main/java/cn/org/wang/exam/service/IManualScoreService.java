package cn.org.wang.exam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import cn.org.wang.exam.common.result.Result;
import cn.org.wang.exam.model.entity.ManualScore;
import cn.org.wang.exam.model.form.answer.CorrectAnswerFrom;
import cn.org.wang.exam.model.vo.answer.AnswerExamVO;
import cn.org.wang.exam.model.vo.answer.UncorrectedUserVO;
import cn.org.wang.exam.model.vo.answer.UserAnswerDetailVO;

import java.util.List;

/**
 * 答卷管理服务接口
 *
 * @author Wang
 * @since 2026-03-21
 */
public interface IManualScoreService extends IService<ManualScore> {
    /**
     * 试卷查询信息
     *
     * @param userId 用户ID
     * @param examId 试卷ID
     * @return
     */
    Result<List<UserAnswerDetailVO>> getDetail(Integer userId, Integer examId);


    /**
     * 批改试卷
     *
     * @param correctAnswerFroms
     * @return
     */
    Result<String> correct(List<CorrectAnswerFrom> correctAnswerFroms);

    /**
     * 分页查找待阅卷考试
     *
     * @return
     */
    Result<IPage<AnswerExamVO>> examPage(Integer pageNum, Integer pageSize, String examName);

    /**
     * 查询待批阅的用户
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param examId 试卷ID
     * @param realName 真实姓名
     * @return
     */
    Result<IPage<UncorrectedUserVO>> stuExamPage(Integer pageNum, Integer pageSize, Integer examId, String realName);

}
