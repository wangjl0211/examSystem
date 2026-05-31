package cn.org.wang.exam.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * IP白名单实体类
 *
 * @author Wang
 * @Version 1.0
 * @Date 2026/5/16
 */
@Data
@Schema(description = "IP白名单实体类")
@TableName("t_ip_whitelist")
public class IpWhitelist implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "IP地址/网段/CIDR")
    private String ipAddress;

    @Schema(description = "类型：1单个IP 2网段 3CIDR")
    private Integer ipType;

    @Schema(description = "描述说明")
    private String description;

    @Schema(description = "状态：1启用 0禁用")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "逻辑删除：0代表未删除，1代表删除")
    private Integer isDeleted;
}
