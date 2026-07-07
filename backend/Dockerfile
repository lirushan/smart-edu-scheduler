# ===========================
# Stage 1: Build with Maven (BuildKit cache mount for .m2)
# ===========================
# syntax=docker/dockerfile:1
FROM maven:3.9-amazoncorretto-21-alpine AS builder

WORKDIR /build
COPY smart-edu-backend/pom.xml .
COPY smart-edu-backend/src ./src
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests -B

# ===========================
# Stage 2: Run with JRE
# ===========================
FROM eclipse-temurin:21-jre-alpine AS runtime

RUN apk add --no-cache curl tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

USER appuser

ENTRYPOINT ["java", \
    "-XX:+UseZGC", \
    "-XX:+ZGenerational", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
