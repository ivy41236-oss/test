# Multi-stage build for Quarkus application
FROM quay.io/quarkus/centos-quarkus-maven:22.3.0-java17 AS build

COPY java-assignment/pom.xml java-assignment/pom.xml
COPY java-assignment/.mvn java-assignment/.mvn
COPY java-assignment/mvnw java-assignment/mvnw
COPY java-assignment/src java-assignment/src

USER root
RUN chown -R quarkus:quarkus /project
USER quarkus

WORKDIR /project/java-assignment
RUN ./mvnw package -DskipTests

# Runtime stage
FROM registry.access.redhat.com/ubi8/ubi-minimal:8.7

# Install necessary runtime dependencies
RUN microdnf update && \
    microdnf install -y java-17-openjdk && \
    microdnf clean all

# Create application user
RUN adduser -G root -u 1000 -s /bin/sh quarkus

# Copy application
COPY --from=build /project/java-assignment/target/*-runner.jar /app/application.jar

# Set permissions
RUN chown -R quarkus:root /app && \
    chmod -R "g+rwX" /app && \
    chown -R quarkus:root /app && \
    chmod -R "g+rwX" /app

USER quarkus

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/q/health || exit 1

CMD ["java", "-jar", "/app/application.jar"]
