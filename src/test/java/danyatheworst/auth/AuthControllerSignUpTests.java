package danyatheworst.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import danyatheworst.auth.dto.RequestSignUpDto;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.stream.Stream;

@WebMvcTest(AuthController.class)
public class AuthControllerSignUpTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegistrationService registrationService;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private SecurityFilterChain securityFilterChain;

    @ParameterizedTest
    @MethodSource("invalidLoginProvider")
    void itShouldReturn400CodeForInvalidLogin(String login) throws Exception {
        RequestSignUpDto signUpDto = new RequestSignUpDto(login, "password");

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/sign-up")
                        .content(this.objectMapper.writeValueAsString(signUpDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.username_validation-error")
                        .value("Login should be between 2 and 50 characters"));
    }

    @ParameterizedTest
    @MethodSource("invalidPasswordProvider")
    void itShouldReturn400CodeForInvalidPassword(String password) throws Exception {
        RequestSignUpDto signUpDto = new RequestSignUpDto("user", password);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/auth/sign-up")
                        .content(this.objectMapper.writeValueAsString(signUpDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.password_validation-error")
                        .value("Password should be between 6 and 50 characters"));
    }

    static Stream<String> invalidLoginProvider() {
        return Stream.of(
                "q".repeat(51),
                "           a          ",
                "   "
        );
    }

    static Stream<String> invalidPasswordProvider() {
        return Stream.of(
                "12345",
                " ".repeat(51)
        );
    }
}
