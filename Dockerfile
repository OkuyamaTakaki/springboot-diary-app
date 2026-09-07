FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY pom.xml mvnw ./
COPY .mvn/ .mvn/
COPY src/ src/

# 起動のたびに入れ子JARを読み込まず、ビルド時に公式の効率配置へ展開する。
# verifyは省略しない。展開はアプリを起動せず、本番DB接続も不要。
RUN chmod +x mvnw && ./mvnw clean verify \
    && java -Djarmode=tools -jar target/myapp-0.0.1-SNAPSHOT.jar extract \
        --destination /app/extracted --application-filename app.jar

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN groupadd --system app && useradd --system --gid app --no-create-home app

COPY --from=build --chown=app:app /app/extracted/ ./

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
