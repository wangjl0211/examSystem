package cn.org.wang.exam.facade;

import cn.org.wang.exam.service.exam.ExamAnswerService;
import cn.org.wang.exam.service.exam.ExamCreateService;
import cn.org.wang.exam.service.exam.ExamQueryService;
import cn.org.wang.exam.service.exam.ExamScoreService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/**
 * 考试门面类
 * 提供统一的考试服务访问入口，供渐进式迁移使用。
 * 生产 API 当前仍经 {@link cn.org.wang.exam.service.impl.ExamServiceImpl}；
 * 交卷计分已统一至 {@link cn.org.wang.exam.service.exam.ExamSubmissionService}。
 *
 * @author Wang
 * @version 1.0
 */
@Component
public class ExamFacade {

    private final ExamCreateService examCreateService;
    private final ExamQueryService examQueryService;
    private final ExamAnswerService examAnswerService;
    private final ExamScoreService examScoreService;

    /**
     * 构造函数
     * @SuppressFBWarnings("EI_EXPOSE_REP2") - Spring依赖注入模式，Service bean是单例且由Spring容器管理，不存在外部修改风险
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public ExamFacade(ExamCreateService examCreateService,
                      ExamQueryService examQueryService,
                      ExamAnswerService examAnswerService,
                      ExamScoreService examScoreService) {
        this.examCreateService = examCreateService;
        this.examQueryService = examQueryService;
        this.examAnswerService = examAnswerService;
        this.examScoreService = examScoreService;
    }

    /**
     * 获取考试创建服务
     *
     * @return ExamCreateService
     * @SuppressFBWarnings("EI_EXPOSE_REP") - 暴露Service以支持门面模式的向后兼容性
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public ExamCreateService create() {
        return examCreateService;
    }

    /**
     * 获取考试查询服务
     *
     * @return ExamQueryService
     * @SuppressFBWarnings("EI_EXPOSE_REP") - 暴露Service以支持门面模式的向后兼容性
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public ExamQueryService query() {
        return examQueryService;
    }

    /**
     * 获取考试答题服务
     *
     * @return ExamAnswerService
     * @SuppressFBWarnings("EI_EXPOSE_REP") - 暴露Service以支持门面模式的向后兼容性
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public ExamAnswerService answer() {
        return examAnswerService;
    }

    /**
     * 获取考试评分服务
     *
     * @return ExamScoreService
     * @SuppressFBWarnings("EI_EXPOSE_REP") - 暴露Service以支持门面模式的向后兼容性
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public ExamScoreService score() {
        return examScoreService;
    }
}