# Currency Exchange Service

## Requirements
- Java 21
- Groovy
- Gradle
- Docker Compose

Application is written to be production ready with env and local configs

## How to start locally
In one terminal window start the docker compose for required dependencies
```
docker-compose -f local-environment-docker-compose.yml up
```

In second terminal window start the application with local profile
```
gradle clean bootRun --args="--spring.profiles.active=local"
```

Check if service is running by hitting the health check endpoint
```
curl http://localhost:8080/currency-exchange/actuator/health
```

For testing run
```
gradle clean test
```

