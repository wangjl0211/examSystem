# 多阶段构建 - 在线考试系统后端
# 构建阶段
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY backend/pom.xml .
# 下载依赖（利用Docker缓存层）
RUN mvn dependency:go-offline -B
COPY backend/src ./src
# 打包应用，跳过测试
RUN mvn clean package -DskipTests -B

# 运行阶段
FROM eclipse-temurin:17-jre-alpine AS runtime
# 添加中文语言支持
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone
# 创建非root用户
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup
WORKDIR /app
# 从构建阶段复制jar包
COPY --from=builder /build/target/exam-1.0-SNAPSHOT.jar app.jar
# 切换到非root用户
USER appuser
# 暴露端口
EXPOSE 8080
# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget -q --spider http://localhost:8080/actuator/health || exit 1
# 启动应用
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
