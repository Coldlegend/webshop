# ==========================================
# 1. Építési fázis (Build Stage)
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Függőségek gyorstárazása (Docker layer cache optimalizálás)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Forráskód bemásolása és a JAR lefordítása (tesztek nélkül a gyors buildért)
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# 2. Éles futtató fázis (Production Runtime)
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Biztonsági hardening: nem futtatunk semmit root felhasználóként
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Csak a kész JAR-t hozzuk át az építő fázisból
COPY --from=builder /build/target/*.jar app.jar

# JVM memória- és konténer-optimalizálási flag-ek éles üzemre
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]