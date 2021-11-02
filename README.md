# Pager: Domain logic
![build](https://github.com/antamb/pager/actions/workflows/test.yaml/badge.svg)

# Overview

The project is a domain logic which aims to handle alert and events for a alert notification system.
The domain logic is implemented using a `hexagonal architecture`.

Some references about hexagonal architecture:
- https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html
- https://netflixtechblog.com/ready-for-changes-with-hexagonal-architecture-b315ec967749
- https://8thlight.com/blog/damon-kelley/2021/05/18/a-color-coded-guide-to-ports-and-adapters.html

# Context

This project has been built with:

- JDK11
- Gradle 7.2
- Spring Boot 2.5.2

## Run tests

```
$ git git@github.com:antamb/pager.git
$ cd pager/
```

### Linux

You'll need java installed

````
./gradlew test --rerun-tasks
````

### Docker

#### Build image

````
docker build -t pager-docker --target test .
````

#### run tests into container

````
docker run -it --rm --name pager-test pager-docker
````

### pipeline

> There is Github actions pipeline for this project [here](https://github.com/antamb/pager/actions/workflows/test.yaml)

