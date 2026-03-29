FROM eclipse-temurin:17-jdk

WORKDIR /app
COPY . .

RUN javac Main.java controller/*.java service/*.java connector/*.java cache/*.java

CMD ["sh", "-c", "java Main"]
