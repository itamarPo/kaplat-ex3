FROM openjdk:17
EXPOSE 3769
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]