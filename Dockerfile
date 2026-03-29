FROM eclipse-temurin:17-jdk

WORKDIR /app
COPY . .

RUN javac $(find . -name "*.java")

CMD ["sh", "-c", "java Main"]
