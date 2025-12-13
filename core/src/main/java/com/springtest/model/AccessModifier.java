package com.springtest.model;

/**
 * Represents the access modifiers available in Java.
 * Defines the visibility level of classes, methods, and fields.
 */
public enum AccessModifier {
    /**
     * Public access - accessible from anywhere.
     */
    PUBLIC,

    /**
     * Protected access - accessible within the same package and subclasses.
     */
    PROTECTED,

    /**
     * Package-private access (default) - accessible only within the same package.
     */
    PACKAGE_PRIVATE,

    /**
     * Private access - accessible only within the same class.
     */
    PRIVATE
}
