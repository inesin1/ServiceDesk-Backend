FROM openjdk:11-jdk

WORKDIR /workdir/server
COPY . .

SHELL ["/bin/bash", "-c"]
RUN sed -i -e 's/\r$//' gradlew

EXPOSE 1002
CMD ./gradlew runFatJar