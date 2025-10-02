# 📦 FlowStep Framework - Library Development & Publishing Guide

## 🎯 Overview

This guide provides a comprehensive roadmap for maintaining and publishing the FlowStep Framework library to public repositories. Use this guide for release planning, community engagement, and ongoing maintenance.

## 📋 Project Phases

### **Phase 1: Library Preparation** (Week 1-2)
### **Phase 2: Open Source Setup** (Week 3)  
### **Phase 3: Repository Publishing** (Week 4-5)
### **Phase 4: Community & Maintenance** (Ongoing)

---

## 🚀 Phase 1: Library Preparation

### **Step 1.1: Project Structure Reorganization**

**Current Structure:**
```
src/main/java/com/example/demo/template/
```

**Target Library Structure:**
```
src/main/java/net/xrftech/flowstep/
├── annotation/
├── context/ 
├── exception/
├── step/
├── QueryTemplate.java
└── CommandTemplate.java
```

**Actions Required:**
```bash
# 1. Create new package structure
mkdir -p src/main/java/io/github/[username]/enterprise-template

# 2. Move all template classes
# 3. Update package declarations
# 4. Update import statements
```

### **Step 1.2: Build Configuration (`build.gradle`)**

```gradle
plugins {
    id 'java-library'
    id 'maven-publish'
    id 'signing'
    id 'io.github.gradle-nexus.publish-plugin' version '1.3.0'
}

group = 'io.github.[username]'
archivesBaseName = 'flowstep-framework'
version = '1.0.0'
description = 'Enterprise-level standardized service framework for Spring Boot with CQRS pattern'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17) // LTS support
    }
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
}

dependencies {
    // Core dependencies (provided scope)
    compileOnly 'org.springframework.boot:spring-boot-starter'
    compileOnly 'org.springframework:spring-tx'
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    
    // Optional dependencies for features
    compileOnly 'org.slf4j:slf4j-api'
    
    // Test dependencies
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.junit.jupiter:junit-jupiter'
    testImplementation 'org.mockito:mockito-core'
    testImplementation 'org.archunit:archunit-junit5:1.0.1'
}

// Publishing configuration
publishing {
    publications {
        maven(MavenPublication) {
            from components.java
            
            pom {
                name = 'FlowStep Framework'
                description = 'A Spring Boot framework implementing CQRS pattern with template method design for enterprise applications'
                url = 'https://github.com/[username]/flowstep-framework'
                
                licenses {
                    license {
                        name = 'MIT License'
                        url = 'https://opensource.org/licenses/MIT'
                    }
                }
                
                developers {
                    developer {
                        id = '[username]'
                        name = '[Full Name]'
                        email = '[email]'
                    }
                }
                
                scm {
                    connection = 'scm:git:git://github.com/[username]/flowstep-framework.git'
                    developerConnection = 'scm:git:ssh://github.com/[username]/flowstep-framework.git'
                    url = 'https://github.com/[username]/flowstep-framework'
                }
            }
        }
    }
}

// Signing for Maven Central
signing {
    required { gradle.taskGraph.hasTask("publish") }
    sign publishing.publications.maven
}

// Nexus publishing
nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}
```

### **Step 1.3: Auto-Configuration for Spring Boot**

Create `src/main/resources/META-INF/spring.factories`:
```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
io.github.[username].enterprisetemplate.config.EnterpriseTemplateAutoConfiguration
```

Create auto-configuration class:
```java
@Configuration
@ConditionalOnClass({QueryTemplate.class, CommandTemplate.class})
public class EnterpriseTemplateAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
    
    // Additional auto-configuration beans
}
```

### **Step 1.4: Comprehensive Testing Suite**

```java
// ArchUnit tests for framework compliance
@AnalyzeClasses(packages = "io.github.[username].enterprisetemplate")
class ArchitectureTest {
    
    @ArchTest
    static final ArchRule enquiry_services_must_be_annotated = 
        classes().that().areAssignableTo(QueryTemplate.class)
                 .should().beAnnotatedWith(QueryFlow.class);
    
    @ArchTest
    static final ArchRule command_services_must_be_transactional =
        classes().that().areAnnotatedWith(CommandFlow.class)
                 .should().beAnnotatedWith(Transactional.class);
}

// Integration tests
@SpringBootTest
class IntegrationTest {
    // Test framework integration with Spring Boot
}
```

### **Step 1.5: Documentation Generation**

```gradle
javadoc {
    if(JavaVersion.current().isJava9Compatible()) {
        options.addBooleanOption('html5', true)
    }
    options.addStringOption('Xdoclint:none', '-quiet')
    options.encoding = 'UTF-8'
    options.charSet = 'UTF-8'
}
```

---

## 🌐 Phase 2: Open Source Setup

### **Step 2.1: Repository Setup**

**GitHub Repository Creation:**
1. Create public repository: `flowstep-framework`
2. Add description: "Enterprise-level Spring Boot framework with CQRS and Template Method patterns"
3. Add topics: `spring-boot`, `cqrs`, `enterprise`, `java`, `template-pattern`

### **Step 2.2: License Selection**

**Recommended: MIT License** (`LICENSE.md`):
```
MIT License

Copyright (c) 2024 [Your Name]

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction...
```

### **Step 2.3: README Creation**

Create comprehensive `README.md`:
```markdown
# 🏗️ FlowStep Framework

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.[username]/flowstep-framework/badge.svg)]()
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)]()
[![Build Status](https://github.com/[username]/flowstep-framework/workflows/CI/badge.svg)]()

## 🎯 Overview
Enterprise-grade Spring Boot framework implementing CQRS pattern with template method design.

## ⚡ Quick Start
```xml
<dependency>
    <groupId>io.github.[username]</groupId>
    <artifactId>flowstep-framework</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 📖 Documentation
- [Getting Started Guide](docs/getting-started.md)
- [API Documentation](docs/api-reference.md)
- [Best Practices](docs/best-practices.md)
```

### **Step 2.4: Contributing Guidelines**

Create `CONTRIBUTING.md`:
```markdown
# Contributing to FlowStep Framework

## Development Setup
1. Fork the repository
2. Clone your fork
3. Run `./gradlew build` to verify setup

## Pull Request Process
1. Create feature branch from `main`
2. Add tests for new features
3. Update documentation
4. Submit PR with clear description

## Code Standards
- Follow existing code style
- Add JavaDoc for public APIs
- Ensure all tests pass
- Run ArchUnit compliance tests
```

### **Step 2.5: GitHub Actions CI/CD**

Create `.github/workflows/ci.yml`:
```yaml
name: CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        java: [11, 17, 21]
    
    steps:
    - uses: actions/checkout@v3
    - name: Set up JDK ${{ matrix.java }}
      uses: actions/setup-java@v3
      with:
        java-version: ${{ matrix.java }}
        distribution: 'temurin'
    
    - name: Cache Gradle packages
      uses: actions/cache@v3
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*') }}
    
    - name: Run tests
      run: ./gradlew build
    
    - name: Run ArchUnit tests
      run: ./gradlew test --tests "*ArchitectureTest*"
```

---

## 📦 Phase 3: Repository Publishing

### **Step 3.1: Maven Central Setup**

**Prerequisites:**
1. Create Sonatype JIRA account: https://issues.sonatype.org
2. Request new project: Create ticket for `io.github.[username]`
3. Verify GitHub ownership
4. Generate GPG key pair for signing

**GPG Setup:**
```bash
# Generate GPG key
gpg --gen-key

# List keys
gpg --list-secret-keys --keyid-format LONG

# Export public key
gpg --armor --export [KEY_ID] > public.key

# Upload to key server
gpg --keyserver keyserver.ubuntu.com --send-keys [KEY_ID]
```

### **Step 3.2: Gradle Configuration for Publishing**

Add to `gradle.properties`:
```properties
signing.keyId=[GPG_KEY_ID]
signing.password=[GPG_PASSWORD]
signing.secretKeyRingFile=[PATH_TO_SECRET_KEY]

ossrhUsername=[SONATYPE_USERNAME]
ossrhPassword=[SONATYPE_PASSWORD]
```

### **Step 3.3: Release Process**

**Automated Release with GitHub Actions** (`.github/workflows/release.yml`):
```yaml
name: Release

on:
  push:
    tags:
      - 'v*'

jobs:
  release:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Import GPG key
      uses: crazy-max/ghaction-import-gpg@v5
      with:
        gpg_private_key: ${{ secrets.GPG_PRIVATE_KEY }}
        passphrase: ${{ secrets.GPG_PASSPHRASE }}
    
    - name: Publish to Maven Central
      run: ./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository
      env:
        ORG_GRADLE_PROJECT_sonatypeUsername: ${{ secrets.SONATYPE_USERNAME }}
        ORG_GRADLE_PROJECT_sonatypePassword: ${{ secrets.SONATYPE_PASSWORD }}
        ORG_GRADLE_PROJECT_signingKey: ${{ secrets.GPG_PRIVATE_KEY }}
        ORG_GRADLE_PROJECT_signingPassword: ${{ secrets.GPG_PASSPHRASE }}
```

### **Step 3.4: GitHub Packages (Alternative)**

```gradle
publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = "https://maven.pkg.github.com/[username]/flowstep-framework"
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

---

## 🎯 Phase 4: Community & Maintenance

### **Step 4.1: Documentation Website**

**GitHub Pages Setup:**
1. Enable GitHub Pages from `docs/` folder
2. Use Jekyll or simple HTML
3. Include:
   - Getting Started guide
   - API documentation  
   - Code examples
   - Best practices

### **Step 4.2: Community Engagement**

**Initial Marketing:**
- Post on Reddit (r/java, r/SpringBoot)
- Share on LinkedIn
- Tweet about release
- Write blog post/article

**Issue Templates** (`.github/ISSUE_TEMPLATE/`):
- Bug report template
- Feature request template
- Question template

### **Step 4.3: Versioning Strategy**

**Semantic Versioning (SemVer):**
- `1.0.0` - Initial release
- `1.0.x` - Patch releases (bug fixes)
- `1.x.0` - Minor releases (new features, backward compatible)
- `x.0.0` - Major releases (breaking changes)

### **Step 4.4: Maintenance Plan**

**Regular Tasks:**
- Dependency updates (monthly)
- Security patches (as needed)
- Spring Boot version compatibility
- Java version support updates
- Community issue response (weekly)

**Release Schedule:**
- Patch releases: As needed for bugs
- Minor releases: Quarterly
- Major releases: Yearly or for breaking changes

---

## 📊 Success Metrics

### **Technical Metrics:**
- [ ] Maven Central sync successful
- [ ] All tests passing on CI
- [ ] Documentation complete
- [ ] Zero critical security vulnerabilities

### **Community Metrics:**
- [ ] 10+ GitHub stars (Month 1)
- [ ] 50+ Maven Central downloads (Month 1)
- [ ] 5+ community contributions (Month 3)
- [ ] Featured in Spring Boot community

### **Quality Gates:**
- [ ] Test coverage > 80%
- [ ] ArchUnit compliance tests pass
- [ ] No SonarCloud critical issues
- [ ] Javadoc coverage > 90%

---

## 🛠️ Tools & Resources

### **Development Tools:**
- **IDE**: IntelliJ IDEA Ultimate
- **Build**: Gradle 8.5+
- **CI/CD**: GitHub Actions
- **Quality**: SonarCloud, CodeQL
- **Documentation**: Javadoc, GitHub Pages

### **Publishing Platforms:**
- **Primary**: Maven Central (Sonatype)
- **Alternative**: GitHub Packages
- **Documentation**: GitHub Pages
- **Community**: GitHub Issues/Discussions

### **Useful Links:**
- [Maven Central Publishing Guide](https://central.sonatype.org/publish/publish-guide/)
- [Gradle Publishing Plugin](https://docs.gradle.org/current/userguide/publishing_maven.html)
- [Spring Boot Starter Guide](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.developing-auto-configuration)

---

## 🎉 Conclusion

Following this guide will result in a professional, enterprise-grade library that:
- ✅ Follows industry best practices
- ✅ Is easily discoverable and usable
- ✅ Has comprehensive documentation
- ✅ Supports community contributions
- ✅ Maintains high quality standards

**Timeline**: 4-5 weeks from start to Maven Central publication.
**Effort**: ~40-60 hours total investment.
**Impact**: Potential to help thousands of enterprise Java developers.

---

*Good luck with your open source journey! 🚀*
