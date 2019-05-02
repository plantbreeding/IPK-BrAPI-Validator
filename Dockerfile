# ----------------------------------------------------------------------
# BUILD BRAPI VALIDATOR WAR FILE
# ----------------------------------------------------------------------
FROM maven as build

WORKDIR /src
COPY . .
RUN touch /src/src/main/resources/config.properties
RUN mvn clean install

# ----------------------------------------------------------------------
# BUILD ACTUAL TOMCAT SERVER WITH ONLY BRAVA
# ----------------------------------------------------------------------
FROM tomcat:alpine

RUN rm -rf /usr/local/tomcat/webapps && mkdir -p /usr/local/tomcat/webapps

COPY --from=build /src/target/BrAPI-validator /usr/local/tomcat/webapps/ROOT/
