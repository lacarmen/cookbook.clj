FROM openjdk:8-alpine

COPY target/uberjar/clj-cookbook.jar /clj-cookbook/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/clj-cookbook/app.jar"]
