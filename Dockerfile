FROM gradle:jdk17-alpine AS build
COPY --chown=gradle:gradle . /src
WORKDIR /src
RUN gradle build

FROM openjdk:17
RUN mkdir /app
COPY --from=build /src/build/libs/trb-core-0.0.1.jar /app/trb-core-0.0.1.jar

ENTRYPOINT ["java", "-jar","/app/trb-core-0.0.1.jar"]