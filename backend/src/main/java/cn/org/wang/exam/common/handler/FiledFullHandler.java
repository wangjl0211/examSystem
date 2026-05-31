package cn.org.wang.exam.common.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;

import cn.org.wang.exam.utils.DateTimeUtil;
import cn.org.wang.exam.utils.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

/**
 * mybatisPlus公共字段填充处理器
 *
 * @author Wang
 * @Version 1.0
 * @Date 2026/3/31 10:00
 */
@Component
@Slf4j
public class FiledFullHandler implements MetaObjectHandler {

    // 常量定义，避免硬编码
    private static final String FIELD_USER_ID = "userId";
    private static final String FIELD_CREATE_TIME = "createTime";

    /**
     * 添加数据拦截
     *
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 没有创建人id就给他自动填充 放属性名而不是字段名
        Class<?> clazz = metaObject.getOriginalObject().getClass();
        Field[] fields = clazz.getDeclaredFields();
        Arrays.stream(fields).forEach(field -> {
            // 填充创建人
            if (FIELD_USER_ID.equals(field.getName()) && (Objects.isNull(getFieldValByName(FIELD_USER_ID, metaObject)))) {
                log.info("user_id字段满足公共字段自动填充规则，已填充");
                this.strictInsertFill(metaObject, FIELD_USER_ID, Integer.class, SecurityUtil.getUserId());

            }
            // 填充创建时间
            if (FIELD_CREATE_TIME.equals(field.getName()) && (Objects.isNull(getFieldValByName(FIELD_CREATE_TIME, metaObject)))) {
                log.info("create_time字段满足公共字段自动填充规则，已填充");
                this.strictInsertFill(metaObject, FIELD_CREATE_TIME, LocalDateTime.class, DateTimeUtil.getDateTime());
            }
        });
    }

    /**
     * 更新数据拦截
     *
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // 当前版本暂不实现更新字段的自动填充
        // 未来可根据业务需求添加更新人、更新时间等字段的自动填充逻辑
    }
}
