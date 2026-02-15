# CI/CD Pipeline Setup

## Overview

This project includes a comprehensive CI/CD pipeline using GitHub Actions, Docker, and Kubernetes.

## CI/CD Pipeline Features

### 🔍 **Testing & Quality Assurance**
- **Unit Tests**: Automated test execution with Maven Surefire
- **Code Coverage**: JaCoCo reports with 80% minimum threshold
- **Security Scanning**: Trivy vulnerability scanner
- **Build Verification**: Maven package validation

### 🐳 **Containerization**
- **Multi-stage Docker Build**: Optimized image size
- **Docker Hub Integration**: Automated image pushing
- **Health Checks**: Container health monitoring

### ☸️ **Deployment**
- **Kubernetes Deployment**: Auto-scaling with 2 replicas
- **Environment Promotion**: Staging → Production workflow
- **Secrets Management**: Secure credential handling
- **Health Probes**: Liveness and readiness checks

## Setup Instructions

### 1. GitHub Secrets

Add these secrets to your GitHub repository:

```bash
# Docker Hub
DOCKER_USERNAME=your-docker-username
DOCKER_PASSWORD=your-docker-password

# Slack (optional)
SLACK_WEBHOOK=your-slack-webhook-url
```

### 2. Local Development

```bash
# Build the application
cd java-assignment
./mvnw clean package

# Build Docker image
docker build -t fulfilment-app:latest .

# Run locally
docker run -p 8080:8080 fulfilment-app:latest
```

### 3. Kubernetes Deployment

```bash
# Apply to cluster
kubectl apply -f k8s/deployment.yaml

# Check status
kubectl get pods -l app=fulfilment-app
kubectl get services
```

## Pipeline Stages

### 1. **Test Stage** (Every push/PR)
- ✅ Code checkout
- ✅ Java 17 setup
- ✅ Maven dependency caching
- ✅ Unit test execution
- ✅ JaCoCo coverage report
- ✅ Code coverage validation (80% threshold)
- ✅ Application build
- ✅ Artifact upload

### 2. **Security Scan** (After tests)
- ✅ Trivy vulnerability scanning
- ✅ SARIF report upload
- ✅ Security findings in GitHub Security tab

### 3. **Deploy Staging** (Main branch only)
- ✅ Docker Buildx setup
- ✅ Docker Hub login
- ✅ Multi-platform image build
- ✅ Image tagging with Git SHA
- ✅ Push to Docker Hub

### 4. **Deploy Production** (Manual approval)
- ✅ Production deployment
- ✅ Slack notification
- ✅ Rollback capability

## Monitoring & Observability

### Health Endpoints
- **Health Check**: `GET /q/health`
- **Readiness**: `GET /q/health/ready`
- **Metrics**: `GET /q/metrics`

### Logging
- Application logs: Available via `kubectl logs`
- Structured logging with correlation IDs
- Log aggregation ready setup

### Alerts
- Build failure notifications
- Security vulnerability alerts
- Deployment status updates

## Performance Optimizations

### Build Performance
- Maven dependency caching
- Docker layer caching
- Parallel test execution

### Runtime Performance
- Resource limits and requests
- Health probe optimization
- Graceful shutdown handling

## Security Features

### Container Security
- Minimal base image (UBI8)
- Non-root user execution
- Security scanning with Trivy

### Application Security
- Secret management
- Environment variable protection
- Dependency vulnerability scanning

## Troubleshooting

### Common Issues

**Build Failures**
```bash
# Check Maven dependencies
./mvnw dependency:tree

# Clean build
./mvnw clean compile
```

**Test Failures**
```bash
# Run specific test
./mvnw test -Dtest=YourTestClass

# Generate detailed report
./mvnw surefire-report:report
```

**Deployment Issues**
```bash
# Check pod status
kubectl describe pod <pod-name>

# Check logs
kubectl logs <pod-name> --follow

# Check events
kubectl get events
```

### Coverage Threshold Issues
- Check JaCoCo report: `target/site/jacoco/index.html`
- Add tests for uncovered code
- Adjust threshold in `pom.xml` if needed

## Best Practices

### Code Quality
- Maintain 80%+ code coverage
- Address security findings promptly
- Follow semantic versioning

### Deployment
- Use feature branches for development
- Test thoroughly in staging
- Monitor production deployments

### Security
- Regular dependency updates
- Security scan review
- Secret rotation

## Future Enhancements

- [ ] Integration tests with Testcontainers
- [ ] Performance testing with K6
- [ ] Blue-green deployments
- [ ] Automated rollback on health check failures
- [ ] Database migration automation
- [ ] Monitoring with Prometheus/Grafana
