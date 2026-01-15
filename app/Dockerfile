# build stage
FROM maven:3.8.8-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

# runtime
FROM tomcat:9.0-jdk11
COPY --from=build /app/target/student-app.war /usr/local/tomcat/webapps/student-app.war
# expose port 8080
EXPOSE 8080