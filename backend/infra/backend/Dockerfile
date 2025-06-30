FROM amazoncorretto:17 AS builder

WORKDIR /app

# Copy build files and gradle wrapper
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle* settings.gradle* ./
COPY src src

# Make sure gradlew is executable
RUN ["chmod", "+x", "gradlew"]

# Build the app
RUN ["./gradlew", "clean", "build", "--no-daemon", "-x", "test"]

FROM amazoncorretto:17
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

# Uncomment and set your database connection details if needed
# ENV SPRING.DATASOURCE.URL=jdbc:mariadb://mariadb-database:3306/mydatabase
# ENV SPRING.DATASOURCE.USERNAME=myuser
# ENV SPRING.DATASOURCE.PASSWORD=password

ENTRYPOINT ["java", "-jar", "app.jar"]
EXPOSE 8080
