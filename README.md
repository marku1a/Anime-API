# Anime-API

## Overview
This is backend for my [Bingeworthy.anime](https://github.com/marku1a/Bingeworthy.anime) application built using Spring Boot, MongoDB, and React. It serves as the backend for a web application focused on trending anime and reviews.

## Table of Contents

- [Demo](#demo)
- [Technologies Used](#technologies-used)
- [Features](#features)
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)

## Demo
![1](https://github.com/marku1a/Bingeworthy.anime/assets/122821687/5359d3ca-c3c2-4178-9d40-b9a90e1374cb)
![3](https://github.com/marku1a/Bingeworthy.anime/assets/122821687/e0fa1e47-001b-4b67-86d7-c0b0f71c3340)
![4](https://github.com/marku1a/Bingeworthy.anime/assets/122821687/9b2572e5-e779-4bf2-af33-e3bbf599bf1c)
![5](https://github.com/marku1a/Bingeworthy.anime/assets/122821687/139f7923-f908-4cb7-b7ca-b077c63e3bc9)
![6](https://github.com/marku1a/Bingeworthy.anime/assets/122821687/6873e58c-9e71-46f7-96fa-4da77c8d9274)


## Technologies Used
- **Spring Boot**: For creating RESTful APIs and handling backend logic.
- **MongoDB**: As the database for storing anime information and user reviews.
- **Spring Security**: For authentication and authorization.
- **JSON Web Tokens (JWT)**: Used for secure communication between the client and the server.
- **Perspective API (by Jigsaw, Google)**: For filtering reviews for profanity/toxicity before posting.
- **Github Actions**: CI/CD for automated testing, building, and deployment of the application.

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

- **GET /api/v1/users**: Retrive all users.
- **PUT /api/v1/users/{userId}/ban**: For locking(banning) user accounts. 
