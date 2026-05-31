package cn.org.wang.exam.model.vo.exam;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @Author Wang
 * @Version
 * @Date 2026/5/20 10:04 AM
 */
@Data
public class OptionVO {
    /**
     * id   选项答案表
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 试题id
     */
    private Integer quId;


    /**
     * 图片地址   0错误 1正确
     */
    private String image;

    /**
     * 选项内容
     */
    @NotBlank(message = "选型内容(content)不能为空")
    private String content;

    private Boolean checkout;
    /**
     * 排序
     */
    private Integer sort;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getQuId() { return quId; }
    public void setQuId(Integer quId) { this.quId = quId; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Boolean getCheckout() { return checkout; }
    public void setCheckout(Boolean checkout) { this.checkout = checkout; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
}

