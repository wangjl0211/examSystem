package cn.org.wang.exam.model.form.question;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

import cn.org.wang.exam.common.exception.ServiceRuntimeException;
import cn.org.wang.exam.model.entity.Option;
import cn.org.wang.exam.utils.excel.ExcelImport;

/**
 * @author Wang
 * @Version 1.0
 * @Date 2026/4/8 10:21
 */
@Data
public class QuestionExcelFrom {
    @ExcelImport(value = "试题类型",required = true)
    private Integer quType;
    @ExcelImport(value = "题干",required = true,unique = true)
    private String content;
    @ExcelImport(value = "解析")
    private String analysis;
    
    // 添加题干图片字段
    @ExcelImport(value = "题干图片")
    private String image;
    
    @ExcelImport(value = "选项一内容")
    private String option1;
    @ExcelImport(value = "选项一是否正确")
    private Integer righted1;
    // 添加选项一图片字段
    @ExcelImport(value = "选项一图片")
    private String image1;
    
    @ExcelImport(value = "选项二内容")
    private String option2;
    @ExcelImport(value = "选项二是否正确")
    private Integer righted2;
    // 添加选项二图片字段
    @ExcelImport(value = "选项二图片")
    private String image2;
    
    @ExcelImport(value = "选项三内容")
    private String option3;
    @ExcelImport(value = "选项三是否正确")
    private Integer righted3;
    // 添加选项三图片字段
    @ExcelImport(value = "选项三图片")
    private String image3;
    
    @ExcelImport(value = "选项四内容")
    private String option4;
    @ExcelImport(value = "选项四是否正确")
    private Integer righted4;
    // 添加选项四图片字段
    @ExcelImport(value = "选项四图片")
    private String image4;
    
    @ExcelImport(value = "选项五内容")
    private String option5;
    @ExcelImport(value = "选项五是否正确")
    private Integer righted5;
    // 添加选项五图片字段
    @ExcelImport(value = "选项五图片")
    private String image5;
    
    @ExcelImport(value = "选项六内容")
    private String option6;
    @ExcelImport(value = "选项六是否正确")
    private Integer righted6;
    // 添加选项六图片字段
    @ExcelImport(value = "选项六图片")
    private String image6;

    public Integer getQuType() { return quType; }
    public void setQuType(Integer quType) { this.quType = quType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getAnalysis() { return analysis; }
    public void setAnalysis(String analysis) { this.analysis = analysis; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getOption1() { return option1; }
    public void setOption1(String option1) { this.option1 = option1; }
    public Integer getRighted1() { return righted1; }
    public void setRighted1(Integer righted1) { this.righted1 = righted1; }
    public String getImage1() { return image1; }
    public void setImage1(String image1) { this.image1 = image1; }
    public String getOption2() { return option2; }
    public void setOption2(String option2) { this.option2 = option2; }
    public Integer getRighted2() { return righted2; }
    public void setRighted2(Integer righted2) { this.righted2 = righted2; }
    public String getImage2() { return image2; }
    public void setImage2(String image2) { this.image2 = image2; }
    public String getOption3() { return option3; }
    public void setOption3(String option3) { this.option3 = option3; }
    public Integer getRighted3() { return righted3; }
    public void setRighted3(Integer righted3) { this.righted3 = righted3; }
    public String getImage3() { return image3; }
    public void setImage3(String image3) { this.image3 = image3; }
    public String getOption4() { return option4; }
    public void setOption4(String option4) { this.option4 = option4; }
    public Integer getRighted4() { return righted4; }
    public void setRighted4(Integer righted4) { this.righted4 = righted4; }
    public String getImage4() { return image4; }
    public void setImage4(String image4) { this.image4 = image4; }
    public String getOption5() { return option5; }
    public void setOption5(String option5) { this.option5 = option5; }
    public Integer getRighted5() { return righted5; }
    public void setRighted5(Integer righted5) { this.righted5 = righted5; }
    public String getImage5() { return image5; }
    public void setImage5(String image5) { this.image5 = image5; }
    public String getOption6() { return option6; }
    public void setOption6(String option6) { this.option6 = option6; }
    public Integer getRighted6() { return righted6; }
    public void setRighted6(Integer righted6) { this.righted6 = righted6; }
    public String getImage6() { return image6; }
    public void setImage6(String image6) { this.image6 = image6; }


    /**
     * 类型转换
     * @param questionExcelFroms 表格获取类型转换成from类型
     * @return 转换后的结果
     */
    public static List<QuestionFrom> converterQuestionFrom(List<QuestionExcelFrom> questionExcelFroms){
        List<QuestionFrom> list = new ArrayList<>(300);
        for (QuestionExcelFrom questionExcelFrom : questionExcelFroms) {
            // 先处理选项，包含对 quType 的非空检查
            List<Option> options = processOptions(questionExcelFrom);
            // 然后创建 QuestionFrom 对象
            QuestionFrom questionFrom = createQuestionFrom(questionExcelFrom);
            questionFrom.setOptions(options);
            list.add(questionFrom);
        }
        return list;
    }

    /**
     * 创建 QuestionFrom 对象
     * @param questionExcelFrom Excel导入的问题对象
     * @return QuestionFrom 对象
     */
    private static QuestionFrom createQuestionFrom(QuestionExcelFrom questionExcelFrom) {
        QuestionFrom questionFrom = new QuestionFrom();
        questionFrom.setContent(questionExcelFrom.getContent());
        questionFrom.setQuType(questionExcelFrom.getQuType());
        questionFrom.setAnalysis(questionExcelFrom.getAnalysis());
        // 设置题干图片
        questionFrom.setImage(questionExcelFrom.getImage());
        return questionFrom;
    }

    /**
     * 处理选项列表
     * @param questionExcelFrom Excel导入的问题对象
     * @return 选项列表
     */
    private static List<Option> processOptions(QuestionExcelFrom questionExcelFrom) {
        List<Option> options = new ArrayList<>();
        
        // 验证选项内容和是否正确的一致性
        validateOptions(questionExcelFrom);
        
        // 添加选项
        addOption(options, questionExcelFrom.getOption1(), questionExcelFrom.getRighted1(), questionExcelFrom.getImage1());
        addOption(options, questionExcelFrom.getOption2(), questionExcelFrom.getRighted2(), questionExcelFrom.getImage2());
        addOption(options, questionExcelFrom.getOption3(), questionExcelFrom.getRighted3(), questionExcelFrom.getImage3());
        addOption(options, questionExcelFrom.getOption4(), questionExcelFrom.getRighted4(), questionExcelFrom.getImage4());
        addOption(options, questionExcelFrom.getOption5(), questionExcelFrom.getRighted5(), questionExcelFrom.getImage5());
        addOption(options, questionExcelFrom.getOption6(), questionExcelFrom.getRighted6(), questionExcelFrom.getImage6());
        
        return options;
    }

    /**
     * 验证选项内容和是否正确的一致性
     * @param questionExcelFrom Excel导入的问题对象
     */
    private static void validateOptions(QuestionExcelFrom questionExcelFrom) {
        Integer quType = questionExcelFrom.getQuType();
        String content = questionExcelFrom.getContent();
        String contentDesc = content != null ? "「" + content + "」" : "空"; 
        if (quType == null) {
            throw new ServiceRuntimeException("导入错误 - 题干为" + contentDesc + "的试题：试题类型不能为空，请检查Excel文件中对应行的「试题类型」列");
        }
        if (quType != 4) { // 非简答题需要验证选项
            validateOption(questionExcelFrom, 1, questionExcelFrom.getOption1(), questionExcelFrom.getRighted1());
            validateOption(questionExcelFrom, 2, questionExcelFrom.getOption2(), questionExcelFrom.getRighted2());
            validateOption(questionExcelFrom, 3, questionExcelFrom.getOption3(), questionExcelFrom.getRighted3());
            validateOption(questionExcelFrom, 4, questionExcelFrom.getOption4(), questionExcelFrom.getRighted4());
            validateOption(questionExcelFrom, 5, questionExcelFrom.getOption5(), questionExcelFrom.getRighted5());
            validateOption(questionExcelFrom, 6, questionExcelFrom.getOption6(), questionExcelFrom.getRighted6());
        }
    }

    /**
     * 验证单个选项
     * @param questionExcelFrom Excel导入的问题对象
     * @param optionIndex 选项索引
     * @param optionContent 选项内容
     * @param isRight 是否正确
     */
    private static void validateOption(QuestionExcelFrom questionExcelFrom, int optionIndex, String optionContent, Integer isRight) {
        if (optionContent != null && !optionContent.isEmpty() && isRight == null) {
            String content = questionExcelFrom.getContent();
            String contentDesc = content != null ? "「" + content + "」" : "空"; 
            String errorMsg = String.format("导入错误 - 题干为%s的试题：选项%s内容存在但未设置是否正确，请检查Excel文件中对应行的「选项%s是否正确」列", 
                    contentDesc, optionIndex, optionIndex);
            throw new ServiceRuntimeException(errorMsg);
        }
    }

    /**
     * 添加选项到列表
     * @param options 选项列表
     * @param content 选项内容
     * @param isRight 是否正确
     * @param image 选项图片
     */
    private static void addOption(List<Option> options, String content, Integer isRight, String image) {
        if (content != null && !content.isEmpty()) {
            Option option = new Option();
            option.setContent(content);
            option.setIsRight(isRight);
            option.setImage(image);
            options.add(option);
        }
    }
}
