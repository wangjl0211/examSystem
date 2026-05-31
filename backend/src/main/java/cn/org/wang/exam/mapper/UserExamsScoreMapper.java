package cn.org.wang.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import cn.org.wang.exam.model.entity.UserExamsScore;
import cn.org.wang.exam.model.vo.answer.UncorrectedUserVO;
import cn.org.wang.exam.model.vo.score.ExportScoreVO;
import cn.org.wang.exam.model.vo.score.SubjectScoreVO;
import cn.org.wang.exam.model.vo.score.UserScoreVO;

import java.util.List;

/**
 * 试卷分数表 Mapper 接口
 *
 * @author Wang
 * @since 2026-03-21
 */
public interface UserExamsScoreMapper extends BaseMapper<UserExamsScore> {

    /**
     * 考试课程用户成绩分析
     *
     * @param page        分页信息
     * @param subjectId     课程Id
     * @param examTitle   考试名称
     * @param userId      用户Id
     * @param roleId      角色Id
     * @param subjectIdList 课程ID集合
     * @return 结果
     */
    IPage<SubjectScoreVO> scoreStatistics(IPage<SubjectScoreVO> page, Integer subjectId, String examTitle, Integer userId,
                                        Integer roleId, List<Integer> subjectIdList);

    /**
     * 成绩分页查询
     *
     * @param page     分页信息
     * @param subjectId  课程Id
     * @param examId   考试Id
     * @param realName 真实姓名
     * @return 查询结果集
     */
    IPage<UserScoreVO> pagingScore(IPage<UserScoreVO> page, Integer subjectId, Integer examId, String realName);

    /**
     * 获取成绩
     *
     * @param examId  考试id
     * @param subjectId 课程id
     * @return 查询结果
     */
    List<ExportScoreVO> selectScores(Integer examId, Integer subjectId);

    /**
     * 根据考试id获取未考试用户
     *
     * @param page   分页信息
     * @param examId 考试id
     * @return 查询结果
     */
    IPage<UncorrectedUserVO> uncorrectedUser(IPage<UncorrectedUserVO> page, Integer examId, String realName);

}
