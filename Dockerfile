FROM openjdk:21-ea-18-jdk-slim
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} shortlink.jar
ENTRYPOINT ["java","-jar","/shortlink.jar"]