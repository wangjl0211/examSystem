package cn.org.wang.exam.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 说明：
 * Swagger配置 (Springdoc)
 *
 * @Author Wang
 * @Version 2.0
 */
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("在线考试系统接口文档")
                        .description("本接口文档阅读对象：WEB服务前后端开发人员")
                        .version("v2.0"));
    }
}
