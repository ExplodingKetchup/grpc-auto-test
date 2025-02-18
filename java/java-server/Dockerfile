FROM eclipse-temurin:21-jre-jammy

ENV HOME=/app
ENV JAR_FILE=java-server-1.0-SNAPSHOT.jar
ARG JAR_FILE_HOST_DIR=./java/java-server/target

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
    appuser && \
    mkdir -p $HOME

WORKDIR $HOME
COPY --chown=$UID --chmod=770 "$JAR_FILE_HOST_DIR/$JAR_FILE" "$HOME/"
RUN chown appuser $HOME && chmod 770 $HOME

USER appuser

EXPOSE 50051
ENTRYPOINT ["sh", "-c", "java -jar $JAR_FILE"]