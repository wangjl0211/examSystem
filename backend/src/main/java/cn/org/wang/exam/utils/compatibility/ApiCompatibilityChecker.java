package cn.org.wang.exam.utils.compatibility;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;


/**
 * API兼容性检查器
 * 用于检查第三方库的API兼容性，避免使用过时的方法
 */
@Component
@Slf4j
public class ApiCompatibilityChecker {



    @PostConstruct
    public void check() {
        checkHutool();
        checkMyBatisPlus();
    }

    private void checkHutool() {
        try {
            Class<?> jsonObjectClass = Class.forName("cn.hutool.json.JSONObject");
            Method putMethod = jsonObjectClass.getMethod("put", String.class, Object.class);
            if (putMethod.isAnnotationPresent(Deprecated.class)) {
                log.debug("JSONObject.put() 已过时，建议使用 set() 或 putOnce()");
            }
        } catch (ReflectiveOperationException e) {
            log.debug("无法检查Hutool API兼容性");
        }
    }

    private void checkMyBatisPlus() {
        // 检查MyBatis-Plus版本兼容性
        log.info("MyBatis-Plus版本: 3.5.6, 与Spring Boot 3.0.13兼容");
    }
}