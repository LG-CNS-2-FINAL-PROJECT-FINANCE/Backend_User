FROM docker.io/amazoncorretto:17
VOLUME /app
EXPOSE 8081
COPY build/libs/*.jar /app.jar
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]