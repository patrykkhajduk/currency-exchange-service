# Currency Exchange Service

## Requirements
- Java 21
- Groovy
- Gradle
- Docker Compose
- Working internet connection when starting

## Description
Creating this application I wanted to show my approach to writing software.</br>
I wanted the application to be as production ready as reasonable, so:</br>
- Extensive request validation is performed
- Tables audition is enabled
- There is logging with tracing and a possibility to use OpenTelemetry connector
- There are main and local properties
- Application is written to be easily scalable

I also wanted to separate getting exchange rates from performing exchanges.</br>
My approach was to create a scheduled job which gets exchange rates and saves them to the database.</br>
This allows for performing exchanges even if NPB API is not working.</br> 
Also, it allows for better results consistency than using caching and an audit trail of all used exchanges.</br>

Please note that due to rounding issues there always will be a small precision loss when exchanging currencies.</br>
Therefore I'm using 7 digits precision for storing results in DB and rounding them to 2 digits when returning to the user.</br>
This way the side effects of rounding are minimized, yet no matter the precision the problem will always remain.</br>


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

## How to use
To perform operations use swagger:http://localhost:8080/currency-exchange/api-spec/swagger-ui/index.html#/
- Create account: http://localhost:8080/currency-exchange/api-spec/swagger-ui/index.html#/account-controller/createAccount
- List accounts: http://localhost:8080/currency-exchange/api-spec/swagger-ui/index.html#/account-controller/findAllAccounts
- Get account details: http://localhost:8080/currency-exchange/api-spec/swagger-ui/index.html#/account-controller/findAccount
- Exchange currencies: http://localhost:8080/currency-exchange/api-spec/swagger-ui/index.html#/account-controller/exchange

For testing run
```
gradle clean test
```

