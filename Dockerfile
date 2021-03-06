# ----------------------------------------------------------------------
# BUILD BRAPI VALIDATOR WAR FILE
# ----------------------------------------------------------------------
FROM maven:3.6-jdk-8 as build

WORKDIR /src
COPY . .
RUN touch /src/src/main/resources/config.properties
RUN mvn clean install -Dhttp.proxyHost="$(echo $(T=${HTTP_PROXY%:*};echo ${T##*/}))" -Dhttp.proxyPort=${HTTP_PROXY##*:} -Dhttps.proxyHost="$(echo $(T=${HTTPS_PROXY%:*};echo ${T##*/}))" -Dhttps.proxyPort=${HTTPS_PROXY##*:}

# ----------------------------------------------------------------------
# BUILD ACTUAL TOMCAT SERVER WITH ONLY BRAVA
# ----------------------------------------------------------------------
FROM tomcat:9-jre8

RUN rm -rf /usr/local/tomcat/webapps && mkdir -p /usr/local/tomcat/webapps

COPY --from=build /src/target/BrAPI-validator /usr/local/tomcat/webapps/ROOT/
