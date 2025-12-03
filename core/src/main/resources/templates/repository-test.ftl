package ${packageName};

<#list imports as import>
    ${import}
</#list>

<#list classAnnotations as annotation>
    ${annotation}
</#list>
class ${className} {

<#list testFields as field>
    ${field}

</#list>

<#list setupMethods as setup>
    ${setup}
</#list>

<#list testCases as testCase>
    @Test
    @DisplayName("${testCase.displayName}")
    void ${testCase.testMethodName}() {
    <#list testCase.givenStatements as given>
        ${given}
    </#list>

    ${testCase.whenStatement}

    <#list testCase.thenStatements as then>
        ${then}
    </#list>
    }

</#list>

private Entity createTestEntity() {
return Entity.builder()
.name("Test")
.build();
}
}