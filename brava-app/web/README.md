# BrAPI Validator (BRAVA) - Web

A simple web application for running validations on a server

Depends on only the [core](../core/README.md) module.

## Use Gradle to run the app

In Windows

```powershell
./gradlew web:bootRun
```

In Linux or MacOS

```shell
./gradle web:bootRun
```


## Build a docker image

To build a build image for development in a server. You will need [https://www.docker.com/](docker) installed for this to work

Run the following

In Windows, Linux or MacOS

```shell
cd web
docker build .
```

Now run the container

In Windows, Linux or MacOS

```shell
cd web
docker build .
```

## Use Docker Compose

To build a run the app on the local computer you can use docker compose. You will need [https://www.docker.com/](docker) installed for this to work

Run the following

In Windows, Linux or MacOS

```powershell
cd web
docker-compose 
```
