package com.springtest.generator;

import com.springtest.model.ClassInfo;

public interface TestGenerator {

    String generateTest(ClassInfo classInfo);

    boolean supports(ClassInfo classInfo);
}
