FROM node:22-alpine AS web
WORKDIR /web
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline
COPY src ./src
COPY --from=web /web/dist ./src/main/resources/static
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:24-jre
RUN useradd --system --create-home --uid 10001 devflow
WORKDIR /app
COPY --from=build /build/target/devflow-*.jar app.jar
USER devflow
ENV JAVA_OPTS="-XX:MaxRAMPercentage=70"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
