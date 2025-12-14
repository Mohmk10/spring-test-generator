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
<#list injectedFields as field>
        // Arrange
        // TODO: Setup mock behavior for ${field.name}
</#list>

        // Act
<#if method.returnsVoid>
        ${instanceName}.${method.name}(${method.args});
<#else>
        ${method.returnType} result = ${instanceName}.${method.name}(${method.args});
</#if>

        // Assert
<#if !method.returnsVoid>
        assertThat(result).isNotNull();
</#if>
        // TODO: Add more assertions
    }

<#if method.throwsExceptions>
    @Test
    void test${method.nameCapitalized}_ThrowsException() {
        // Arrange
        // TODO: Setup mocks to throw exception

        // Act & Assert
        assertThatThrownBy(() -> ${instanceName}.${method.name}(${method.args}))
            .isInstanceOf(${method.exceptionType}.class);
    }

</#if>
</#list>
}
