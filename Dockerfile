
#COPY target/*$profile.jar application-$profile.jar

FROM maven:3.8.4-openjdk-17-slim
ARG profile

# image layer
WORKDIR /app
ADD pom.xml /app
RUN mvn verify clean --fail-never
COPY . /app
RUN mvn -v
#RUN mvn install -DskipTests
RUN mvn package -P $profile
#EXPOSE 8080
ENTRYPOINT ["java", "-jar", "target/file-server-2.5.6.jar"]