FROM gradle:9.3-jdk25 as builder

WORKDIR /app
COPY settings.gradle.kts ./
COPY build.gradle.kts ./
COPY gradle ./gradle

RUN gradle dependencies --no-daemon

COPY scylladb ./scylladb
COPY src ./src

RUN gradle clean build

FROM amazoncorretto:25-jdk

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 9393

HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD curl -f http://localhost:9393/actuator/health || exit 1


ENTRYPOINT ["java", \
  "-XX:+UseG1GC", \
  "-XX:MaxGCPauseMillis=200", \
  "-XX:+ParallelRefProcEnabled", \
  "-jar", "app.jar"]

