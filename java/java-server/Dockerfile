FROM eclipse-temurin:21-jre-jammy

ENV HOME=/app
ARG JAR_FILE_HOST_DIR=./java/java-server/target
ARG JAR_FILE=java-server-1.0-SNAPSHOT.jar

# Create a non-privileged user that the app will run under.
# See https://docs.docker.com/go/dockerfile-user-best-practices/
ARG UID=10001
RUN adduser \
    --disabled-password \
    --gecos "" \
    --home "/nonexistent" \
    --shell "/sbin/nologin" \
    --no-create-home \
    --uid "${UID}" \
    appuser

RUN mkdir -p $HOME
WORKDIR $HOME
COPY "$JAR_FILE_HOST_DIR/$JAR_FILE" .
RUN chown appuser $HOME
RUN chmod 770 $HOME

USER appuser

EXPOSE 50051
ENTRYPOINT ["java", "-jar", "$JAR_FILE"]
