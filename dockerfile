# 멀티스테이지 빌드
FROM gradle:8.5-jdk21-alpine AS builder

WORKDIR /app

# Gradle 캐시 최적화를 위해 의존성 파일만 먼저 복사
COPY build.gradle settings.gradle ./
COPY gradle gradle

# 의존성 다운로드
RUN gradle dependencies --no-daemon || true

# 소스 코드 복사
COPY src src

# 빌드 실행 (테스트 제외)
RUN gradle clean build -x test --no-daemon

# 런타임 스테이지
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 빌더 스테이지에서 생성된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 비root 사용자로 실행
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# 환경 변수 설정
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]