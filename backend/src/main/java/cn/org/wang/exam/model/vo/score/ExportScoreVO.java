package cn.org.wang.exam.model.vo.score;

import cn.org.wang.exam.utils.excel.ExcelExport;
import lombok.Data;

/**
 * 成绩导出VO
 * 定义Excel导出的列顺序和表头名称
 *
 * @author Wang
 * @Version 1.0
 * @Date 2026/4/22 10:12
 */
@Data
public class ExportScoreVO {

    @ExcelExport(value = "姓名", sort = 0)
    private String realName;

    @ExcelExport(value = "课程", sort = 1)
    private String subjectName;

    @ExcelExport(value = "分数", sort = 2)
    private Double score;

    @ExcelExport(value = "名次", sort = 3)
    private Integer ranking;

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getsubjectName() { return subjectName; }
    public void setsubjectName(String subjectName) { this.subjectName = subjectName; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public Integer getRanking() { return ranking; }
    public void setRanking(Integer ranking) { this.ranking = ranking; }
}
