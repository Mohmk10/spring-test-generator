package com.springtest.core.naming;

import com.springtest.core.model.MethodInfo;

public interface NamingStrategy {

    String generateTestMethodName(MethodInfo method, String scenario);

    String generateDisplayName(MethodInfo method, String scenario);
}