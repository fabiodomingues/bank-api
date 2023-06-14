FROM openjdk:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/bank-api-0.0.1-SNAPSHOT-standalone.jar /bank-api/app.jar

EXPOSE 8080

CMD ["java", "-jar", "/bank-api/app.jar"]
