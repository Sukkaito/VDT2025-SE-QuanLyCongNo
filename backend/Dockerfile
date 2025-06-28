FROM openjdk:17-oracle
WORKDIR .
RUN ["gradlew", "clean", "build", "--no-daemon"]
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]