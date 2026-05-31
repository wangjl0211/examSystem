package cn.org.wang.exam.model.vo.score;

import lombok.Data;


/**
 * @author Wang
 * @Version 1.0
 * @Date 2026/4/22 9:23
 */

@Data
public class QuestionAnalyseVO {
    // 正确数量
    private Integer rightCount;
    // 总题数
    private Integer totalCount;
    private Double accuracy;

    public Integer getRightCount() { return rightCount; }
    public void setRightCount(Integer rightCount) { this.rightCount = rightCount; }
    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
    public Double getAccuracy() { return accuracy; }
    public void setAccuracy(Double accuracy) { this.accuracy = accuracy; }
}
