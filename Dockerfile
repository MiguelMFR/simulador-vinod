FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:17-jre-jammy

RUN apt-get update \
    && apt-get install --yes --no-install-recommends \
        curl \
        fluxbox \
        novnc \
        websockify \
        x11vnc \
        xvfb \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /build/target/simulador-mic1-1.0.0.jar ./app.jar
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN sed -i 's/\r$//' /usr/local/bin/docker-entrypoint.sh \
    && chmod +x /usr/local/bin/docker-entrypoint.sh

EXPOSE 6080

ENV DISPLAY=:99
ENV JAVA_TOOL_OPTIONS="-Djava.awt.headless=false"
ENV SCREEN_WIDTH=1600
ENV SCREEN_HEIGHT=900

HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
    CMD curl --fail --silent http://localhost:6080/vnc.html >/dev/null || exit 1

ENTRYPOINT ["docker-entrypoint.sh"]
CMD ["java", "-jar", "/app/app.jar"]
