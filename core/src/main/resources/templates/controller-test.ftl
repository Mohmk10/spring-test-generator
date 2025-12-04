package ${packageName};

<#list imports as import>
    import ${import};
</#list>

@WebMvcTest(${className}.class)
class ${testClassName} {

@Autowired
private MockMvc mockMvc;

<#list mockBeans as mockBean>
    @MockBean
    private ${mockBean.type} ${mockBean.name};

</#list>
<#if needsObjectMapper>
    @Autowired
    private ObjectMapper objectMapper;

</#if>
<#list testMethods as method>
    @Test
    @DisplayName("${method.displayName}")
    void ${method.name}() throws Exception {
    ${method.body}
    }

</#list>
}