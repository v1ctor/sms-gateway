FROM gradle:6.8.3-jdk11 as builder

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

RUN gradle shadowJar --no-daemon

FROM openjdk:11-jre-slim
COPY --from=builder /home/gradle/src/build/libs/sms-gateway.jar /app/
WORKDIR /app
ENTRYPOINT ["java", "-server", "-jar", "sms-gateway.jar"]


