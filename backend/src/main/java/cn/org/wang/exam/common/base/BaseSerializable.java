package cn.org.wang.exam.common.base;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;

/**
 * 序列化基类
 */
public abstract class BaseSerializable implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // 提供版本控制方法
    public Long getSerialVersion() {
        return serialVersionUID;
    }
    
    // 序列化验证
    public boolean validateSerialization() {
        try {
            // 使用Jackson进行序列化验证
            ObjectMapper objectMapper = new ObjectMapper();
            byte[] bytes = objectMapper.writeValueAsBytes(this);
            Object obj = objectMapper.readValue(bytes, this.getClass());
            return obj != null && obj.getClass().equals(this.getClass());
        } catch (IOException e) {
            return false;
        }
    }
}