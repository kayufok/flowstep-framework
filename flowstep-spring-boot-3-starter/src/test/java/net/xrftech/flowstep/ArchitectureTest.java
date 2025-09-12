package net.xrftech.flowstep;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 * Architecture tests using ArchUnit - Optional dependency
 * 
 * These tests only run when:
 * 1. ArchUnit is on the classpath
 * 2. System property 'flowstep.archunit.enabled' is set to 'true'
 * 
 * To enable: ./gradlew test -PenableArchUnit=true
 */
@EnabledIfSystemProperty(named = "flowstep.archunit.enabled", matches = "true")
class ArchitectureTest {

    @Test
    void archUnitTestsRequireExplicitEnabling() {
        // This test will only run when ArchUnit is explicitly enabled
        // Check if ArchUnit classes are available
        try {
            Class.forName("com.tngtech.archunit.core.domain.JavaClasses");
            // If we get here, ArchUnit is available - run actual tests
            runArchUnitTests();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("ArchUnit is not available on classpath. " +
                "Add 'testImplementation \"com.tngtech.archunit:archunit-junit5:1.2.1\"' " +
                "to your build.gradle to enable architecture testing.");
        }
    }

    private void runArchUnitTests() {
        // Import ArchUnit classes only when we know they're available
        try {
            // Use reflection to avoid compile-time dependency
            Class<?> classFileImporterClass = Class.forName("com.tngtech.archunit.core.importer.ClassFileImporter");
            Class<?> javaClassesClass = Class.forName("com.tngtech.archunit.core.domain.JavaClasses");
            Class<?> archRuleClass = Class.forName("com.tngtech.archunit.lang.ArchRule");
            Class<?> archRuleDefinitionClass = Class.forName("com.tngtech.archunit.lang.syntax.ArchRuleDefinition");
            
            // Create ClassFileImporter instance
            Object classFileImporter = classFileImporterClass.getDeclaredConstructor().newInstance();
            
            // Import packages
            Object importedClasses = classFileImporterClass
                .getMethod("importPackages", String[].class)
                .invoke(classFileImporter, new Object[]{new String[]{"net.xrftech.flowstep"}});
            
            // Run basic architecture rules
            runBasicArchitectureRules(importedClasses, archRuleDefinitionClass);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to run ArchUnit tests", e);
        }
    }

    private void runBasicArchitectureRules(Object importedClasses, Class<?> archRuleDefinitionClass) throws Exception {
        // Basic test: QueryTemplate should exist and be public
        Object classesRule = archRuleDefinitionClass.getMethod("classes").invoke(null);
        Object thatRule = classesRule.getClass().getMethod("that").invoke(classesRule);
        Object containingRule = thatRule.getClass()
            .getMethod("haveSimpleNameContaining", String.class)
            .invoke(thatRule, "QueryTemplate");
        Object shouldRule = containingRule.getClass().getMethod("should").invoke(containingRule);
        Object finalRule = shouldRule.getClass().getMethod("bePublic").invoke(shouldRule);
        
        // Execute the rule
        finalRule.getClass().getMethod("check", Class.forName("com.tngtech.archunit.core.domain.JavaClasses"))
            .invoke(finalRule, importedClasses);
        
        System.out.println("✅ Basic architecture rules passed");
    }
}