package ${packageName};

<#list imports as import>
    import ${import};
</#list>

@DataJpaTest
<#if useTestcontainers>
    @AutoConfigureTestDatabase(replace = Replace.NONE)
    @Testcontainers
</#if>
class ${testClassName} {

<#if useTestcontainers>
    @Container
    static ${databaseType?cap_first}Container<?> ${databaseType} = new ${databaseType?cap_first}Container<>("${databaseType}:${databaseType == 'postgresql' ? '16' : '8'}")
    .withDatabaseName("testdb")
    .withUsername("test")
    .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", ${databaseType}::getJdbcUrl);
    registry.add("spring.datasource.username", ${databaseType}::getUsername);
    registry.add("spring.datasource.password", ${databaseType}::getPassword);
    }

</#if>
@Autowired
private ${className} repository;

@Autowired
private TestEntityManager entityManager;

<#if needsSetup>
    @BeforeEach
    void setUp() {
    repository.deleteAll();
    }

</#if>
<#list testMethods as method>
    @Test
    @DisplayName("${method.displayName}")
    void ${method.name}() {
    ${method.body}
    }

</#list>
}