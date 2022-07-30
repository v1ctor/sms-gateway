FROM --platform=$BUILDPLATFORM gradle:jdk17 as builder

COPY . /home/gradle/src
WORKDIR /home/gradle/src

RUN ./gradlew shadowJar --no-daemon

FROM --platform=$BUILDPLATFORM openjdk:17

COPY --from=builder /home/gradle/src/build/libs/sms-gateway.jar /app/
WORKDIR /app
ENTRYPOINT ["java", "-server", "-jar", "sms-gateway.jar"]


