# Identity service
This microservice is responsible for:
* Onboarding users
* Roles and permissions
* Authentication

## Tech stack
* Build tool: maven >= 3.9.5
* Java: 17
* Framework: Spring boot 3.2.x
* DBMS: MySQL

## Prerequisites
* Java SDK 17
* A MySQL server

## Start application
`mvn spring-boot:run`

## Build application
`mvn clean package`

## Docker guideline
### Build docker image
`docker build -t <account>/identity-service:0.0.1 .`
### Push docker image to Docker Hub
`docker image push <account>/identity-service:0.0.1`
### Create network:
`docker network create tien-network`
### Start MySQL in tien-network
`docker run --network tien-network --name mysql-ms-identity -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root -d mysql:8.0.36-debian`
### Run your application in tien-network
`docker run --name identity-service --network tien-network -p 8080:8080 -e DBMS_URL=jdbc:mysql://mysql-ms-identity:3306/identity_service identity-service:0.0.1`

## Install Docker on ubuntu
Follow steps in this [link](https://docs.docker.com/engine/install/ubuntu/#install-using-the-repository)