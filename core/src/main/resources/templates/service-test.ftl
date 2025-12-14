package ${packageName};

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import ${classQualifiedName};
<#list injectedFields as field>
<#if field.qualifiedType?? && !field.qualifiedType?starts_with("java.")>
import ${field.qualifiedType};
</#if>
</#list>

@ExtendWith(MockitoExtension.class)
class ${className}Test {

<#list injectedFields as field>
    @Mock
    private ${field.type} ${field.name};

</#list>
    @InjectMocks
    private ${className} ${instanceName};

<#list methods as method>
    @Test
    void test${method.nameCapitalized}() {
<#if method.returnsVoid>
        ${instanceName}.${method.name}(${method.args});
<#else>
        ${method.returnType} result = ${instanceName}.${method.name}(${method.args});
<#if !method.returnsVoid>
        assertThat(result).isNotNull();
</#if>
</#if>
    }

<#if method.throwsExceptions>
    @Test
    void test${method.nameCapitalized}_ThrowsException() {
        assertThatThrownBy(() -> ${instanceName}.${method.name}(${method.args}))
            .isInstanceOf(${method.exceptionType}.class);
    }

</#if>
</#list>
}
