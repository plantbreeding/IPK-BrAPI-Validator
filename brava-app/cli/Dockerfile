MAINTAINER brapi.org
FROM gradle:8.0.2-jdk8-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
COPY . src
WORKDIR /home/gradle/src
RUN gradle cli:installDist --no-daemon

FROM openjdk:11

RUN mkdir /app

COPY --from=build /home/gradle/src/build/install/brava /app

ENTRYPOINT /usr/app/brava/bin/brava