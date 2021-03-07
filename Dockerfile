FROM gradle:6.8.3-jdk11 as builder

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

RUN gradle shadowJar --no-daemon

ENV TELEGRAM_TOKEN=
ENV DB_CONNECTION=jdbc:sqlite:/data/data.db
ENV MODEM_PASSWORD=
ENV MODEM_USERNAME=admin
ENV ALLOWED_USERS=

FROM openjdk:11-jre-slim
COPY --from=builder /home/gradle/src/build/libs/sms-gateway.jar /app/
WORKDIR /app
ENTRYPOINT java -server $JAVA_OPTS -jar sms-gateway.jar --token $TELEGRAM_TOKEN --db_connection $DB_CONNECTION --username $MODEM_USERNAME --password $MODEM_PASSWORD --allowed_users $ALLOWED_USERS


