FROM eclipse-temurin:17-alpine

WORKDIR /app

COPY target/anime-0.0.1-SNAPSHOT.jar /app/Anime-API.jar

EXPOSE 8080

CMD ["java", "-jar", "Anime-API.jar"]