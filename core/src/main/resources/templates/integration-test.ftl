package ${packageName};

<#list imports as import>
    import ${import};
</#list>

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
<#if useTestcontainers>
    @Testcontainers
</#if>
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ${testClassName} {

<#if useTestcontainers>
    @Container
    static ${databaseType?cap_first}Container<?> ${databaseType} = new ${databaseType?cap_first}Container<>("${databaseType}:${databaseType == 'postgresql' ? '16' : '8'}");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", ${databaseType}::getJdbcUrl);
    registry.add("spring.datasource.username", ${databaseType}::getUsername);
    registry.add("spring.datasource.password", ${databaseType}::getPassword);
    }

</#if>
@Autowired
private MockMvc mockMvc;

@Autowired
private ${className}Repository repository;

@Autowired
private ObjectMapper objectMapper;

@BeforeEach
void setUp() {
repository.deleteAll();
}

<#list testMethods as method>
    @Test
    @Order(${method.order})
    @DisplayName("${method.displayName}")
    void ${method.name}() throws Exception {
    ${method.body}
    }

</#list>
}