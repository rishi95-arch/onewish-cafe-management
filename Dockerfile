FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B package

FROM tomcat:10.1-jdk17-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/onewish.war /usr/local/tomcat/webapps/ROOT.war
COPY docker-entrypoint.sh /usr/local/bin/onewish-entrypoint
RUN chmod 755 /usr/local/bin/onewish-entrypoint
EXPOSE 8080
ENTRYPOINT ["/usr/local/bin/onewish-entrypoint"]
