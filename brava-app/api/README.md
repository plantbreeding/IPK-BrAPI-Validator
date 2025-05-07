# BrAPI Validator (BRAVA) - RESTful API 

A RESTful API for running BRAVA

Depends on the [core](../core/README.md) and [database](../jpa/README.md) modules.

## Use Gradle to run the app

The default mode uses a JPA connection to a database. 

In Windows

```powershell
./gradlew api:bootRun
```

In Linux or MacOS

```shell
./gradle api:bootRun
```

However if you like to use the MongoDb version use 

In Windows

```powershell
./gradlew api:bootRun -Pmongodb
```

In Linux or MacOS

```shell
./gradle api:bootRun -Pmongodb
```