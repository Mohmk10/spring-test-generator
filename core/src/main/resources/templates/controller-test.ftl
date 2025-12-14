package ${packageName};

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import org.springframework.http.MediaType;

import java.util.List;
import java.util.Optional;

import ${classQualifiedName};
<#list injectedFields as field>
<#if field.qualifiedType?? && !field.qualifiedType?starts_with("java.")>
import ${field.qualifiedType};
</#if>
</#list>

@WebMvcTest(${className}.class)
class ${className}Test {

    @Autowired
    private MockMvc mockMvc;

<#list injectedFields as field>
    @MockBean
    private ${field.type} ${field.name};

</#list>

<#list webMethods as method>
    @Test
    void test${method.nameCapitalized}() throws Exception {
        // Arrange
<#list injectedFields as field>
        // TODO: Setup mock behavior for ${field.name}
</#list>

        // Act & Assert
        mockMvc.perform(${method.httpMethod?lower_case}("${method.path}")
<#if method.httpMethod == "POST" || method.httpMethod == "PUT" || method.httpMethod == "PATCH">
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
</#if>
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())<#if !method.returnsVoid>
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").exists())</#if>;
    }

    @Test
    void test${method.nameCapitalized}_ReturnsError() throws Exception {
        // Arrange
        // TODO: Setup mocks to cause error

        // Act & Assert
        mockMvc.perform(${method.httpMethod?lower_case}("${method.path}")
<#if method.httpMethod == "POST" || method.httpMethod == "PUT" || method.httpMethod == "PATCH">
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
</#if>
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().is4xxClientError());
    }

</#list>
}
