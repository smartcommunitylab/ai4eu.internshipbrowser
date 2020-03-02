# syntax=docker/dockerfile:experimental
FROM maven:3-jdk-11 AS mvn
WORKDIR /tmp
COPY . /tmp
RUN --mount=type=bind,target=/root/.m2,source=/root/.m2,from=smartcommunitylab/ai4eu-internshipbrowser:cache-alpine  mvn clean install -Dmaven.test.skip=true


FROM adoptopenjdk/openjdk11:alpine
ENV FOLDER=/tmp/target
ARG VER=0.1
ENV APP=internshipbrowser-${VER}.jar
ARG USER=ai4eu
ARG USER_ID=3006
ARG USER_GROUP=ai4eu
ARG USER_GROUP_ID=3006
ARG USER_HOME=/home/${USER}

RUN  addgroup -g ${USER_GROUP_ID} ${USER_GROUP}; \
     adduser -u ${USER_ID} -D -g '' -h ${USER_HOME} -G ${USER_GROUP} ${USER} ;

WORKDIR  /home/${USER}/app
RUN chown ${USER}:${USER_GROUP} /home/${USER}/app
RUN mkdir indexes && chown ${USER}:${USER_GROUP} indexes
RUN apk add --no-cache tzdata
COPY --from=mvn --chown=ai4eu:ai4eu ${FOLDER}/${APP} /home/${USER}/app/${APP}

USER ai4eu
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar ${APP}"]