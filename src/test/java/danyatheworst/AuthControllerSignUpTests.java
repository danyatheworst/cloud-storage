package danyatheworst;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import danyatheworst.auth.AuthController;
import danyatheworst.auth.RegistrationService;
import danyatheworst.auth.RequestSignUpDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;


@WebMvcTest(AuthController.class)
public class AuthControllerSignUpTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegistrationService registrationService;

    @MockBean
    private SecurityFilterChain securityFilterChain;

    @Test
    void itShouldReturn409CodeWhenLoginConsistsOfMoreThanFiftyCharacters() throws Exception {
        //given
        String login = "q".repeat(51);
        RequestSignUpDto signUpDto = new RequestSignUpDto(login, "password");

        //when and then
        this.mockMvc
                .perform(buildRequest(signUpDto))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(this.loginValidationMessageError());
    }

    @Test
    void itShouldReturn409CodeWhenTrimmedLoginConsistsOfLessThanTwoCharacters() throws Exception {
        //given
        String login = "           a          ";
        RequestSignUpDto signUpDto = new RequestSignUpDto(login, "password");

        //when and then
        this.mockMvc
                .perform(buildRequest(signUpDto))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(this.loginValidationMessageError());
    }

    @Test
    void itShouldReturn409CodeWhenLoginConsistsOfWhiteSpacesOnly() throws Exception {
        //given
        String login = "   ";
        RequestSignUpDto signUpDto = new RequestSignUpDto(login, "password");

        //when and then
        this.mockMvc
                .perform(buildRequest(signUpDto))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(this.loginValidationMessageError());
    }

    @Test
    void itShouldReturn409CodeWhenPasswordConsistsOfLessThanSixCharacters() throws Exception {
        //given
        String password = "12345";
        RequestSignUpDto signUpDto = new RequestSignUpDto("user", password);

        //when and then
        this.mockMvc
                .perform(buildRequest(signUpDto))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(this.passwordValidationMessageError());
    }

    @Test
    void itShouldReturn409CodeWhenPasswordConsistsOfMoreThanFiftyCharacters() throws Exception {
        //given
        String password = " ".repeat(51);
        RequestSignUpDto signUpDto = new RequestSignUpDto("user", password);

        //when and then
        this.mockMvc
                .perform(buildRequest(signUpDto))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(this.passwordValidationMessageError());
    }

    private MockHttpServletRequestBuilder buildRequest(RequestSignUpDto signUpDto) throws JsonProcessingException {
        return MockMvcRequestBuilders
                .post("/auth/sign-up")
                .content(this.objectMapper.writeValueAsString(signUpDto))
                .contentType(MediaType.APPLICATION_JSON);
    }

    private ResultMatcher loginValidationMessageError() {
       return MockMvcResultMatchers.jsonPath("$.login_validation-error")
               .value("Login should be between 2 and 50 characters");
    }

    private ResultMatcher passwordValidationMessageError() {
        return MockMvcResultMatchers.jsonPath("$.password_validation-error")
                .value("Password should be between 6 and 50 characters");
    }
}
