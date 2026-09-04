FROM eclipse-temurin:21-jdk AS build

WORKDIR /workdir/server
COPY gradle gradle
COPY gradlew gradle.properties settings.gradle.kts build.gradle.kts ./
RUN ./gradlew --no-daemon dependencies

COPY src src
RUN ./gradlew --no-daemon buildFatJar

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /workdir/server/build/libs/*-all.jar app.jar

EXPOSE 1002
CMD ["java", "-jar", "app.jar"]
