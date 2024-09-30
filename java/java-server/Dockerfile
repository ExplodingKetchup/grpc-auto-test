FROM eclipse-temurin:21.0.4_7-jre

ENV HOME=/app
ARG JAR_FILE=java-server-1.0-SNAPSHOT.jar

RUN mkdir -p $HOME
WORKDIR $HOME
COPY "./target/$JAR_FILE" .
RUN mkdir -p "$HOME/log"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "$JAR_FILE"]