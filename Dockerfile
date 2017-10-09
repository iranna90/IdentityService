FROM java:8

COPY target/IdentityProvider-1.0-SNAPSHOT-shaded.jar /tmp/Application/application.jar

WORKDIR /tmp/Application

EXPOSE 8080

CMD ["java", "-Xmx2G", "-jar", "application.jar"]
