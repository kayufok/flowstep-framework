# FlowStep Spring Boot 2 Starter

This module provides FlowStep framework support for:
- **Java 8+**
- **Spring Boot 2.7.x+**
- **Legacy Spring applications**

## Dependencies

```xml
<dependency>
    <groupId>net.xrftech</groupId>
    <artifactId>flowstep-spring-boot-2-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Features

- ✅ Java 8+ compatibility
- ✅ Spring Boot 2.7.x auto-configuration
- ✅ javax.validation support
- ✅ Optional ArchUnit testing
- ✅ Legacy environment support

## Configuration

Uses Spring Boot 2.x configuration format:

```properties
flowstep.enabled=true
flowstep.exception-handler.enabled=true
```

## Architecture Testing

Optional ArchUnit support:

```bash
./gradlew :flowstep-spring-boot-2-starter:test -PenableArchUnit=true
```

## Compatibility

| Component | Version |
|-----------|---------|
| Java | 8+ |
| Spring Boot | 2.7.x+ |
| Spring Framework | 5.3.x+ |
| Validation API | javax.validation 2.0+ |

For modern applications with Java 17+ and Spring Boot 3.x, use `flowstep-spring-boot-3-starter` instead.