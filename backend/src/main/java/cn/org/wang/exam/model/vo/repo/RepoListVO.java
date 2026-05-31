package cn.org.wang.exam.model.vo.repo;

import lombok.Data;

/**
 * @author Wang
 * @Version 1.0
 * @Date 2026/6/3 21:38
 */
@Data
public class RepoListVO {
    // 题库ID
    private Integer id;

    // 题库标题
    private String title;

    // 单选题数量
    private Integer radioNum;

    // 多选题数量
    private Integer multiNum;

    // 判断题数量
    private Integer judgeNum;

    // 简答题数量
    private Integer saqNum;
}
