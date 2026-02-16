# CI/CD Pipeline Guide

## Overview

This repository uses GitHub Actions for CI/CD.
The current pipeline focuses on test/quality/security validation.

## Current Workflow Status

- CI test and coverage checks are enabled.
- Security scan (Trivy + SARIF upload) is enabled.
- Deployment jobs are intentionally disabled.
  - `deploy-staging`: `if: false`
  - `deploy-production`: `if: false`

## Workflow File

- `.github/workflows/ci.yml`

## Pipeline Stages

### 1) Test Job (`test`)

Runs on push/PR:

- Checkout code
- Make Maven wrapper executable (`chmod +x java-assignment/mvnw`)
- Set up JDK 17
- Cache Maven dependencies
- Run tests (`./mvnw clean test`)
- Generate JaCoCo report
- Upload coverage to Codecov
- Run coverage gate via Maven verify (`./mvnw verify`)
- Build package (`./mvnw clean package -DskipTests`)
- Upload build artifact

Coverage gate:

- JaCoCo check configured in `java-assignment/pom.xml`
- Bundle instruction coverage minimum: `0.80` (80%)

### 2) Security Scan Job (`security-scan`)

- Runs after test job
- Executes Trivy filesystem scan
- Uploads SARIF using `github/codeql-action/upload-sarif@v3`
- Uses `security-events: write` permission
- SARIF upload is skipped for fork PRs (permission-safe condition)

### 3) Deploy Jobs (`deploy-staging`, `deploy-production`)

- Present in workflow, but currently disabled by `if: false`
- No Docker push or production deployment is performed

## Required/Optional Secrets

### Optional (currently not used because deploy jobs are disabled)

- `DOCKER_USERNAME`
- `DOCKER_PASSWORD`
- `SLACK_WEBHOOK`

If deployment jobs are re-enabled later, set these secrets first.

## Local Commands

From `java-assignment` directory:

```bash
./mvnw clean test
./mvnw verify
./mvnw clean package -DskipTests
```

## Troubleshooting

### 1) `./mvnw: Permission denied` (Linux CI)

Ensure wrapper is executable:

```bash
chmod +x java-assignment/mvnw
```

### 2) JaCoCo coverage check fails

- Open report: `java-assignment/target/site/jacoco/index.html`
- Add tests for uncovered classes
- Re-run:

```bash
./mvnw clean verify
```

### 3) SARIF upload permission error

- Ensure CodeQL action version is `v3`
- Ensure job permissions include:
  - `contents: read`
  - `security-events: write`

