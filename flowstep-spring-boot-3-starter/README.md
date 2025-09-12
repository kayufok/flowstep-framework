# FlowStep Spring Boot 3 Starter

This module provides FlowStep framework support for:
- **Java 17+**
- **Spring Boot 3.x+**
- **Modern Spring applications**

## Dependencies

```xml
<dependency>
    <groupId>net.xrftech</groupId>
    <artifactId>flowstep-spring-boot-3-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Features

- ✅ Java 17+ LTS compatibility
- ✅ Spring Boot 3.x auto-configuration
- ✅ Jakarta EE validation support
- ✅ Optional ArchUnit testing
- ✅ Modern Spring features

## Configuration

Uses Spring Boot 3.x configuration format:

```properties
flowstep.enabled=true
flowstep.exception-handler.enabled=true
```

## Architecture Testing

Optional ArchUnit support:

```bash
./gradlew :flowstep-spring-boot-3-starter:test -PenableArchUnit=true
```

## Compatibility

| Component | Version |
|-----------|---------|
| Java | 17+ |
| Spring Boot | 3.x+ |
| Spring Framework | 6.x+ |
| Validation API | jakarta.validation 3.0+ |

For legacy applications with Java 8+ and Spring Boot 2.7.x, use `flowstep-spring-boot-2-starter` instead.