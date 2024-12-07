FROM alpine:3.21
COPY ./ .
RUN apk add openjdk17
RUN --mount=type=secret,id=gh_packages_username \
    --mount=type=secret,id=gh_packages_password \
    ORG_GRADLE_PROJECT_ghPackagesUsername=$(cat /run/secrets/gh_packages_username) \
    ORG_GRADLE_PROJECT_ghPackagesPwd=$(cat /run/secrets/gh_packages_password) \
    ./gradlew compileJava
ENTRYPOINT ./gradlew run