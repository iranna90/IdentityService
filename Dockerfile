FROM phusion/baseimage:latest

COPY target/IdentityProvider-1.0-SNAPSHOT.jar /tmp/Application/application.jar

WORKDIR /tmp/Application

EXPOSE 8080

RUN echo $PATH

CMD ["java", "-Xmx2G", "-jar", "application.jar"]
