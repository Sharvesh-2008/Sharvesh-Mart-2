# SharveshMart - Optimized multi-stage Dockerfile for Railway (alternative to Nixpacks)
# Use only if you switch railway.toml builder to "dockerfile"
# Build: docker build -t sharveshmart . && docker run -p 8080:8080 -e PORT=8080 sharveshmart

# ---------- Stage 1: Build ----------
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app
# Cache deps first (copy pom only)
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline
# Copy source and build
COPY src ./src
COPY db ./db
RUN mvn -B -T 1C -DskipTests clean package -Dmaven.test.skip=true && \
    mv target/SharveshMart-*.jar target/app.jar && \
    java -Djarmode=layertools -jar target/app.jar extract --destination target/extracted

# ---------- Stage 2: Runtime ----------
FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app
RUN addgroup --system app && adduser --system --ingroup app app
# Copy extracted layers for best Docker cache (Spring Boot layered jar)
COPY --from=builder /app/target/extracted/dependencies/ ./
COPY --from=builder /app/target/extracted/spring-boot-loader/ ./
COPY --from=builder /app/target/extracted/snapshot-dependencies/ ./
COPY --from=builder /app/target/extracted/application/ ./
# H2 file DB lives in /app/data (Railway volume is ephemeral - ok for demo)
RUN mkdir -p /app/data && chown -R app:app /app
USER app
EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-Xms128m -Xmx512m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseStringDeduplication"
# Railway injects $PORT
CMD ["java", "-Dserver.port=${PORT}", "org.springframework.boot.loader.JarLauncher"]
# For non-layered fallback (if extract fails), uncomment:
# COPY --from=builder /app/target/app.jar app.jar
# CMD ["java", "-Xms128m", "-Xmx512m", "-XX:+UseContainerSupport", "-Dserver.port=${PORT}", "-jar", "app.jar"]
