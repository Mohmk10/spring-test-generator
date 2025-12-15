package com.springtest.gradle;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

public interface SpringTestGeneratorExtension {

    Property<String> getSourceDirectory();

    Property<String> getOutputDirectory();

    Property<String> getTestType();

    Property<String> getNamingStrategy();

    ListProperty<String> getIncludes();

    ListProperty<String> getExcludes();
}
