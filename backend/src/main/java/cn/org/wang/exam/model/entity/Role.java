package cn.org.wang.exam.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 角色实体类
 *
 * @ Author JinXi
 * @ Version 1.0
 * @ Date 2026/4/25 14:10
 */
@Data
@Schema(description ="角色实体类")
@TableName("t_role")
public class Role implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description ="角色ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description ="角色名称")
    private String roleName;

    @Schema(description ="角色编码")
    private String code;
}

