# ===== Стадия 1: сборка JAR =====
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Сначала только файлы сборки: если они не менялись,
# Docker возьмёт скачанные зависимости из кэша и не будет качать их заново
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

# Затем исходный код и сама сборка (вместе с тестами)
COPY src ./src
RUN ./gradlew build --no-daemon

# ===== Стадия 2: запуск =====
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Из первой стадии копируется только готовый JAR
COPY --from=build /app/build/libs/hello-spring-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080 8081
ENTRYPOINT ["java", "-jar", "app.jar"]