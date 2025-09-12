package net.xrftech.flowstep.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark query services in the system.
 * Used for architectural governance and service identification.
 * 
 * Services annotated with @QueryFlow should:
 * - Inherit from QueryTemplate
 * - Be read-only operations (no side effects)
 * - Not require transactions
 * - Return pure business objects
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface QueryFlow {
    /**
     * Unique code identifying this query service
     * @return service code
     */
    String code();
    
    /**
     * Human-readable description of this query service
     * @return service description
     */
    String desc();
}
