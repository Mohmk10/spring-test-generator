package com.springtest.mock;

import com.springtest.model.MethodInfo;
import com.springtest.model.ParameterInfo;
import com.springtest.model.AccessModifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StubGeneratorTest {

    private StubGenerator stubGenerator;

    @BeforeEach
    void setUp() {
        stubGenerator = new StubGenerator();
    }

    @Test
    void shouldGenerateWhenThenReturn_WhenMethodHasNoParameters() {

        MethodInfo method = new MethodInfo(
                "getUser",
                "String",
                "java.lang.String",
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        String result = stubGenerator.generateWhenThenReturn("userService", method);

        assertThat(result).isEqualTo("when(userService.getUser()).thenReturn(\"test\");");
    }

    @Test
    void shouldGenerateWhenThenReturn_WhenMethodHasParameters() {

        ParameterInfo param = new ParameterInfo("id", "Long", "java.lang.Long", List.of(), false, null);
        MethodInfo method = new MethodInfo(
                "findById",
                "User",
                "com.example.User",
                List.of(param),
                List.of(),
                List.of(),
                List.of(),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        String result = stubGenerator.generateWhenThenReturn("userRepository", method);

        assertThat(result).contains("when(userRepository.findById(anyLong())).thenReturn(null);");
    }

    @Test
    void shouldGenerateWhenThenThrow_WhenMethodCanThrowException() {

        MethodInfo method = new MethodInfo(
                "deleteUser",
                "void",
                "void",
                List.of(),
                List.of(),
                List.of("IOException"),
                List.of(),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        String result = stubGenerator.generateWhenThenThrow("userService", method, "RuntimeException");

        assertThat(result).contains("when(userService.deleteUser()).thenThrow(new RuntimeException(\"Test exception\"));");
    }

    @Test
    void shouldGenerateDoNothing_WhenMethodReturnsVoid() {

        MethodInfo method = new MethodInfo(
                "updateUser",
                "void",
                "void",
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        String result = stubGenerator.generateDoNothing("userService", method);

        assertThat(result).contains("doNothing().when(userService.updateUser());");
    }

    @Test
    void shouldGenerateDoThrow_WhenVoidMethodShouldThrow() {

        MethodInfo method = new MethodInfo(
                "deleteUser",
                "void",
                "void",
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        String result = stubGenerator.generateDoThrow("userService", method, "IllegalStateException");

        assertThat(result).contains("doThrow(new IllegalStateException(\"Test exception\")).when(userService.deleteUser());");
    }

    @Test
    void shouldGenerateArgumentMatchers_WhenMethodHasVariousParameters() {

        List<ParameterInfo> params = List.of(
                new ParameterInfo("name", "String", "java.lang.String", List.of(), false, null),
                new ParameterInfo("age", "int", "int", List.of(), false, null),
                new ParameterInfo("active", "boolean", "boolean", List.of(), false, null)
        );

        MethodInfo method = new MethodInfo(
                "createUser",
                "User",
                "com.example.User",
                params,
                List.of(),
                List.of(),
                List.of(),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        String result = stubGenerator.generateWhenThenReturn("userService", method);

        assertThat(result).contains("anyString()");
        assertThat(result).contains("anyInt()");
        assertThat(result).contains("anyBoolean()");
    }

    @Test
    void shouldGenerateMultipleReturns_WhenMethodCalledMultipleTimes() {

        MethodInfo method = new MethodInfo(
                "getCount",
                "int",
                "int",
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                false,
                AccessModifier.PUBLIC,
                false,
                false
        );

        String result = stubGenerator.generateMultipleReturns("counterService", method, List.of("1", "2", "3"));

        assertThat(result).contains("when(counterService.getCount()).thenReturn(1, 2, 3);");
    }

    @Test
    void shouldGenerateDefaultReturnValues_ForCommonTypes() {

        MethodInfo stringMethod = new MethodInfo("getString", "String", "java.lang.String",
                List.of(), List.of(), List.of(), List.of(), false, AccessModifier.PUBLIC, false, false);
        MethodInfo intMethod = new MethodInfo("getInt", "int", "int",
                List.of(), List.of(), List.of(), List.of(), false, AccessModifier.PUBLIC, false, false);
        MethodInfo listMethod = new MethodInfo("getList", "List", "java.util.List",
                List.of(), List.of(), List.of(), List.of(), false, AccessModifier.PUBLIC, false, false);

        String stringStub = stubGenerator.generateWhenThenReturn("service", stringMethod);
        String intStub = stubGenerator.generateWhenThenReturn("service", intMethod);
        String listStub = stubGenerator.generateWhenThenReturn("service", listMethod);

        assertThat(stringStub).contains("\"test\"");
        assertThat(intStub).contains("0");
        assertThat(listStub).contains("List.of()");
    }

    @Test
    void shouldGenerateAllStubs_WhenProvidedMultipleMethods() {

        MethodInfo method1 = new MethodInfo("findAll", "List", "java.util.List",
                List.of(), List.of(), List.of(), List.of(), false, AccessModifier.PUBLIC, false, false);
        MethodInfo method2 = new MethodInfo("save", "void", "void",
                List.of(), List.of(), List.of(), List.of(), false, AccessModifier.PUBLIC, false, false);

        List<String> stubs = stubGenerator.generateAllStubs("repository", List.of(method1, method2));

        assertThat(stubs).hasSize(2);
        assertThat(stubs.get(0)).contains("findAll");
        assertThat(stubs.get(1)).contains("save");
    }

    @Test
    void shouldGenerateImports_WhenGeneratingStubs() {

        List<String> imports = stubGenerator.generateImports();

        assertThat(imports).contains("import static org.mockito.Mockito.*;");
        assertThat(imports).contains("import static org.mockito.ArgumentMatchers.*;");
    }
}
