FROM openjdk:17-jdk-slim

WORKDIR /app

COPY . .

RUN javac Main.java

CMD ["java", "Main"]
