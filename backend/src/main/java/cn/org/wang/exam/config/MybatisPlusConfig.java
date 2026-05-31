package cn.org.wang.exam.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;

import cn.org.wang.exam.common.handler.FiledFullHandler;
import jakarta.annotation.Resource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MybatisPlus配置类
 * 配置分页插件、元数据处理器、SQL执行拦截器等
 *
 * @Author Wang
 * @Version
 * @Date 2026/3/28 3:57 PM
 */
@Configuration
// mapper 包扫描已在 ExamApplication.java 中配置
public class MybatisPlusConfig {

    /**
     * 字段自动填充处理器
     */
    @Resource
    private FiledFullHandler filedFullHandler;

    /**
     * 配置MybatisPlus拦截器
     * 主要配置分页插件
     *
     * @return MybatisPlusInterceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加MySQL分页插件
        interceptor.addInnerInterceptor(new
                PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * 配置全局配置
     * 主要配置元数据对象处理器，用于自动填充创建时间、更新时间等字段
     *
     * @return GlobalConfig
     */
    @Bean
    public GlobalConfig globalConfig() {
        GlobalConfig config = new GlobalConfig();
        // 设置元数据对象处理器
        config.setMetaObjectHandler(filedFullHandler);
        return config;
    }

    /**
     * 配置SQL执行拦截器
     * 用于统计和监控SQL执行情况
     *
     * @return SqlExecutionInterceptor
     */
    @Bean
    public SqlExecutionInterceptor sqlExecutionInterceptor() {
        return new SqlExecutionInterceptor();
    }

    /**
     * 配置Bean后置处理器
     * 用于在SqlSessionFactory初始化后注册SQL执行拦截器
     *
     * @return BeanPostProcessor
     */
    @Bean
    public BeanPostProcessor sqlSessionFactoryBeanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                // 检查是否为SqlSessionFactory
                if (bean instanceof SqlSessionFactory sqlSessionFactory) {
                    // 注册SQL执行拦截器到Mybatis配置中
                    sqlSessionFactory.getConfiguration().addInterceptor(sqlExecutionInterceptor());
                }
                return bean;
            }
        };
    }
}

