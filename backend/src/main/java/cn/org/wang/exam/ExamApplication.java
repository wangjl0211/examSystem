package cn.org.wang.exam;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import io.github.cdimascio.dotenv.Dotenv;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * SpringBoot启动类
 *
 * @Author Wang
 * @Version 1.0
 * @Date 2026/3/25 11:20 AM
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableAsync
@EnableScheduling
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 1800 * 2 )
@MapperScan("cn.org.wang.exam.mapper")
public class ExamApplication {
    public static void main(String[] args) {
        // 从命令行参数获取 .env 文件路径
        String envPath = System.getProperty("env.file", ".env");
        
        // 使用 java.nio.file.Path 解析路径
        String directory = ".";
        String filename = envPath;
        
        try {
            Path path = Paths.get(envPath);
            Path parent = path.getParent();
            Path fileName = path.getFileName();
            if (parent != null) {
                directory = parent.toString();
            }
            if (fileName != null) {
                filename = fileName.toString();
            }
        } catch (Exception e) {
            // 路径解析失败，使用默认值
            System.err.println("路径解析失败，使用默认值: " + e.getMessage());
        }
        
        // 加载 .env 文件
        Dotenv dotenv = Dotenv.configure()
                .directory(directory)
                .filename(filename)
                .load();
        
        // 将环境变量设置到系统属性
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        
        SpringApplication.run(ExamApplication.class, args);
    }
}