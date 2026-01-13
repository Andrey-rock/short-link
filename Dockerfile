FROM eclipse-temurin:21-jre-alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} shortlink.jar
ENTRYPOINT ["java","-jar","/shortlink.jar"]