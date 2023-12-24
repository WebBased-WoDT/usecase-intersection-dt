FROM alpine:3.19
COPY ./ .
RUN apk add openjdk17
ENTRYPOINT ./gradlew run