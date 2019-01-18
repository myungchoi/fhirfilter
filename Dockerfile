#Build the Maven project
FROM maven:3.5.2-alpine as builder
COPY . /usr/src/app
WORKDIR /usr/src/app
RUN ./mvnw package

#Build the Tomcat container
FROM openjdk:8-jdk-alpine
RUN apk update
RUN apk add zip

# Copy fhirFilter war file to webapps.
COPY --from=builder /usr/src/app/target/fhirfilter.war fhirfilter.jar
EXPOSE 8080

ENTRYPOINT ["java","-jar","/fhirfilter.jar"]