package danyatheworst.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import danyatheworst.storage.service.FileStorageService;
import danyatheworst.user.User;
import danyatheworst.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest
public class SignUpIntegrationTests {
    @Container
    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:14-alpine")
                    .withDatabaseName("database-test")
                    .withUsername("username-test")
                    .withPassword("password-test");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private FileStorageService fileStorageService;

    @AfterEach
    public void cleanUserTable() {
        this.userRepository.deleteAll();
    }

    @Test
    public void itShouldInsertUserIntoDatabaseAndReturn201StatusCode() throws Exception {
        //given
        String login = "user";
        RequestSignUpDto signUpDto = new RequestSignUpDto(login, "password");

        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/sign-up")
                        .content(this.objectMapper.writeValueAsString(signUpDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated());

        Assertions.assertEquals(this.userRepository.findAll().size(), 1);
        Assertions.assertEquals(this.userRepository.findByUsername(login).get().getUsername(), login);
    }

    @Test
    void itShouldReturn409StatusCodeWhenUserAlreadyExists() throws Exception {
        //given
        String login = "user";
        RequestSignUpDto signUpDto = new RequestSignUpDto(login, "password");
        this.userRepository.save(new User(login, "password"));

        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/sign-up")
                        .content(this.objectMapper.writeValueAsString(signUpDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("That username is taken. Try another"));
    }
}
