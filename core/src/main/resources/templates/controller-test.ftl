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
    void ${testCase.testMethodName}() throws Exception {
    <#list testCase.givenStatements as given>
        ${given}
    </#list>

    <#list testCase.mockSetups as mock>
        ${mock}
    </#list>

    ${testCase.whenStatement}
    <#list testCase.thenStatements as then>
        ${then}
    </#list>;
    }

</#list>
}