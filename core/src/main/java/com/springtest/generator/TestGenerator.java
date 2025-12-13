package com.springtest.generator;

import com.springtest.model.ClassInfo;

/**
 * Common interface for all test generators.
 * Implementations generate complete Java test files for different types of Spring components.
 */
public interface TestGenerator {

    /**
     * Generates a complete test class for the given class information.
     * The generated test should be a valid, compilable Java class with appropriate
     * test annotations, setup methods, and test cases.
     *
     * @param classInfo Information about the class to generate tests for
     * @return Complete Java test file content as a String
     * @throws IllegalArgumentException if classInfo is null or invalid
     */
    String generateTest(ClassInfo classInfo);

    /**
     * Checks if this generator supports the given class type.
     * Used to determine which generator should be used for a specific class.
     *
     * @param classInfo Information about the class
     * @return true if this generator can generate tests for the given class
     */
    boolean supports(ClassInfo classInfo);
}
