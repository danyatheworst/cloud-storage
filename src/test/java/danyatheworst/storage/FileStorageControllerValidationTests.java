package danyatheworst.storage;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.stream.Collectors;
import java.util.stream.Stream;


@WebMvcTest(FileStorageController.class)
public class FileStorageControllerValidationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private SecurityFilterChain securityFilterChain;

    @ParameterizedTest
    @CsvSource({
            "'/', 'Path is invalid.'",
            "'///////', 'Path is invalid.'",
            "'folder/.anotherFolder', 'File name can''t start with a dot.'",
            "'folder/some\\folder', 'File name can''t contain a backslash.'",
            "'folder/some\nolder', 'Path can''t contain an escape sequence.'",
            "'folder/fo:lder', 'File name can''t contain a colon.'",
    })
    public void itShouldReturn400StatusCodeAndSpecificMessage(String path, String expectedMessage) throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.post("/directories")
                        .param("path", path)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status()
                        .isBadRequest())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$.message")
                        .value(expectedMessage));
    }

    @ParameterizedTest
    @CsvSource({
            "0, 'path size must be between 1 and 255'",
            "256, 'path size must be between 1 and 255'"
    })
    void itShouldReturnBadRequestForInvalidPathLength(int length, String expectedMessage) throws Exception {
        String path = generateStringOfLength(length);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/directories")
                        .param("path", path)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status()
                        .isBadRequest())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$.message")
                        .value(expectedMessage));
    }

    private String generateStringOfLength(int length) {
        return Stream.generate(() -> "a").limit(length).collect(Collectors.joining());
    }
}