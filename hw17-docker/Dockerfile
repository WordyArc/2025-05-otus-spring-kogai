# syntax=docker/dockerfile:1
# check=error=true

# Build
########################################################
FROM maven:3.9.11-eclipse-temurin-17-noble AS build
WORKDIR /workspace

RUN --mount=type=cache,id=maven-cache,target=/root/.m2 true

COPY pom.xml ./

RUN --mount=type=cache,id=maven-cache,target=/root/.m2 \
    mvn -B -q -DskipTests dependency:go-offline

COPY src ./src

RUN --mount=type=cache,id=maven-cache,target=/root/.m2 \
    mvn -B -DskipTests package && \
    mv target/*.jar /workspace/app.jar

RUN java -Djarmode=layertools -jar /workspace/app.jar extract --destination /workspace/extracted



# Runtime
########################################################
FROM eclipse-temurin:17-jre-noble AS runtime

RUN useradd -u 10001 -m -s /usr/sbin/nologin spring
WORKDIR /app

COPY --from=build --chown=spring:spring /workspace/extracted/dependencies/ ./
COPY --from=build --chown=spring:spring /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=spring:spring /workspace/extracted/spring-boot-loader/ ./
COPY --from=build --chown=spring:spring /workspace/extracted/application/ ./

VOLUME /tmp

LABEL org.opencontainers.image.title="spring-boot-app" \
      org.opencontainers.image.description="Spring Boot on Temurin 17 (Ubuntu 24.04 Noble)" \
      org.opencontainers.image.vendor="Viktor Kogai"

ENV TZ=UTC \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_TOOL_OPTIONS="-XX:InitialRAMPercentage=25.0 -XX:MaxRAMPercentage=75.0 -XX:+UseStringDeduplication -XX:+ExitOnOutOfMemoryError"

EXPOSE 8080

RUN mkdir -p /app/logs && chown -R spring:spring /app/logs

USER spring

ENTRYPOINT ["java","org.springframework.boot.loader.launch.JarLauncher"]
