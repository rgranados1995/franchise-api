# franchise-api

Reactive REST API to manage a network of franchises, their branches and the products each branch stocks.

## Tech stack

- Java 17, Spring Boot 4 with Spring WebFlux (non-blocking end to end)
- Clean Architecture generated with the Bancolombia scaffold (`co.com.bancolombia.cleanArchitecture` 5.2.1)
- Gradle 9.9.1 through the wrapper

## Prerequisites

- JDK 17 (the build uses a Java 17 toolchain even if a newer JDK runs Gradle)
- Docker (needed from the persistence stage onwards)

## Build and test

```bash
./gradlew build jacocoMergedReport
```

On Windows use `.\gradlew` instead of `./gradlew`.

Reports:

- Coverage: `build/reports/jacoco/jacocoMergedReport/html/index.html`
- Mutation testing: `build/reports/pitest/`

## Project structure

| Module | Layer | Responsibility |
|---|---|---|
| `domain/model` | Domain | Entities, business rules and ports (gateways) |
| `domain/usecase` | Domain | Use cases that orchestrate the business rules |
| `applications/app-service` | Application | Spring Boot entry point and bean wiring |

## Design decisions

| Decision | Reason |
|---|---|
| Java 17 | Required runtime for the project; LTS release |
| Gradle wrapper jar is versioned | Anyone can build from a clean clone without installing Gradle. `distributionSha256Sum` in `gradle-wrapper.properties` makes the wrapper reject a tampered distribution |
