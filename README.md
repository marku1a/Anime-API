# Anime-API

## Overview
This is backend for my [Bingeworthy.anime](https://github.com/marku1a/Bingeworthy.anime) application built using Spring Boot, MongoDB, and React. It serves as the backend for a web application focused on trending anime and reviews.

## Technologies Used
- **Spring Boot**: For creating RESTful APIs and handling backend logic.
- **MongoDB**: As the database for storing anime information and user reviews.
- **Spring Security**: For authentication and authorization.
- **JSON Web Tokens (JWT)**: Used for secure communication between the client and the server.

## Features
- **Anime Endpoint**: Allows retrieval, creation, updating, and deletion of anime data.
- **User Authentication**: Provides endpoints for user registration, login, and token refresh.
- **Authorization**: Role-based access control using Spring Security annotations.
- **Review Management**: Allows users to create and view reviews for anime.

## Getting Started
To run the project locally, follow these steps:

1. Ensure you have Java and MongoDB installed (or MongoDB Atlas)
2. Clone this repository to your local machine.
3. Configure MongoDB connection settings in `application.properties`
(create .env file and put in required fields like in .env.example)
4. Build the project using Maven: `mvn clean install`
5. Fill the MongoDB Anime collection with Anime.json file from Data folder
6. Run the application: `java -jar target/anime-0.0.1-SNAPSHOT.jar`

Using Docker:
1. Clone Anime-API and Bingeworthy.anime and put them in same folder
2. Using CMD navigate to Anime-API folder - `cd path/to/your/Anime-API`
3. Use Docker Compose to build the images and run the containers - `docker compose up`
4. Don't forget to fill MongoDB Anime collection with Anime.json file from Data folder

## API Endpoints
- **GET /api/v1/anime**: Retrieve all anime.
- **GET /api/v1/anime/{id}**: Retrieve an anime by its ID.
- **POST /api/v1/anime/create-anime**: Create a new anime.
- **PUT /api/v1/anime/{id}**: Update an existing anime.
- **DELETE /api/v1/anime/{id}**: Delete an anime.

- **POST /api/v1/auth/register**: Register a new user.
- **POST /api/v1/auth/authenticate**: Authenticate a user.
- **POST /api/v1/auth/refresh-token**: Refresh the authentication token.

- **POST /api/v1/anime-reviews**: Create a new review.
- **GET /api/v1/anime-reviews/{imdbId}**: Retrieve reviews for a specific anime.
