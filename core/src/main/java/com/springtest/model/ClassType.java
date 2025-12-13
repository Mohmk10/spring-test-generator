package com.springtest.model;

/**
 * Represents the type of Spring component class.
 * Used to categorize classes based on their Spring annotations and roles.
 */
public enum ClassType {
    /**
     * Service layer component - typically annotated with @Service.
     */
    SERVICE,

    /**
     * Controller layer component - typically annotated with @Controller or @RestController.
     */
    CONTROLLER,

    /**
     * Repository layer component - typically annotated with @Repository.
     */
    REPOSITORY,

    /**
     * Generic Spring component - typically annotated with @Component.
     */
    COMPONENT,

    /**
     * Configuration class - typically annotated with @Configuration.
     */
    CONFIGURATION,

    /**
     * Other class type - not a recognized Spring stereotype.
     */
    OTHER
}
