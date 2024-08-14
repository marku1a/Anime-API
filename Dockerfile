FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/anime-0.0.1-SNAPSHOT.jar ./Anime-API.jar
EXPOSE 8080
CMD ["java", "-jar", "Anime-API.jar"]