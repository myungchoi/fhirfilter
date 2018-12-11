#Build the Maven project
FROM maven:3.5.2-alpine as builder
COPY . /usr/src/app
WORKDIR /usr/src/app
RUN mvn install

#Build the Tomcat container
FROM tomcat:alpine
RUN apk update
RUN apk add zip

# Copy GT-FHIR war file to webapps.
COPY --from=builder /usr/src/app/target/fhirfilter.war $CATALINA_HOME/webapps/fhirfilter.war

EXPOSE 8080
