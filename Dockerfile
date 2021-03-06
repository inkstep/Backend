FROM maven:3.6.1-jdk-11

EXPOSE 4567

# Copy files needed
COPY src/ src/
COPY run run
COPY checkstyle.xml checkstyle.xml
COPY pom.xml pom.xml
COPY email/ email/

RUN mvn compile assembly:single

# Start Main
CMD java -jar target/inkstep-1.0.jar


