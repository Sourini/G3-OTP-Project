FROM maven:3.9.9-eclipse-temurin-21-jammy

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
       libgtk-3-0 libxtst6 libxi6 libxrender1 libgl1 libasound2 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn --batch-mode -DskipTests package

ENV JAVA_TOOL_OPTIONS="-Dprism.order=sw"

CMD ["mvn", "--batch-mode", "javafx:run"]