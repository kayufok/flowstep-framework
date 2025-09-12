package net.xrftech.flowstep.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark write/command services in the system.
 * Used for architectural governance and service identification.
 * 
 * Services annotated with @CommandFlow should:
 * - Inherit from CommandTemplate
 * - Handle create/update/delete operations
 * - Be annotated with @Transactional
 * - Support audit information
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CommandFlow {
    /**
     * Unique code identifying this command service
     * @return service code
     */
    String code();
    
    /**
     * Human-readable description of this command service
     * @return service description
     */
    String desc();
}
