# Contributing to SHARVESHMART

Exact steps from `git clone` to a running local instance.

## 1. Clone

```bash
git clone <repository-url> SHARVESHMART
cd SHARVESHMART
```

## 2. Prerequisites

- JDK 17 (`java -version`)
- Maven 3.9+ (`mvn -version`)
- Apache Tomcat 9.0.x (any recent 9.0.x)

On Windows, `JAVA_HOME` must point at a JDK 17, e.g.
`set JAVA_HOME=C:\Program Files\Java\jdk-17.0.19`.

## 3. Local configuration

```bash
cp src/main/resources/config.properties.example src/main/resources/config.properties
# edit jdbc.url / jdbc.username / jdbc.password as needed
```

The schema and seed data are applied automatically on first application startup,
so no manual database setup is required for local development.

## 4. Build and test

```bash
mvn -B clean verify
```

This runs unit + DAO tests, Checkstyle, and SpotBugs.

## 5. Run locally

```bash
mvn -B clean package
cp target/SHARVESHMART.war <TOMCAT_HOME>/webapps/
<TOMCAT_HOME>/bin/startup.sh
```

Then open http://localhost:8080/SHARVESHMART/. Seed accounts are listed in the README.

## Branch model & commits

- `main` is always deployable.
- Work on `feature/<name>` branches; merge via a self-reviewed pull request.
- Conventional commits: `feat:`, `fix:`, `test:`, `docs:`, `build:`, `chore:`.
- Minimum 3 commits per week across the checkpoint window (Jul 27 – Oct 10).

## Definition of Done (spec Section 19)

- [ ] Compiles with no Checkstyle/SpotBugs major warnings
- [ ] Unit/DAO tests written and passing
- [ ] Code self-reviewed before merge
- [ ] Migration script included if the schema changed
- [ ] Verified against the deployed URL, not only localhost
- [ ] README/API documentation updated if behavior changed
- [ ] Merged to `main` only when CI is green
