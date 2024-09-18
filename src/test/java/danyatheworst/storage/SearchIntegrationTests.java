package danyatheworst.storage;



import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class SearchIntegrationTests extends AbstractFileStorageIntegrationTests {

    @Test
    void itShouldReturn200StatusCodeWhenNothingWasFoundUsingSearch() throws Exception {
        String query = "nonExistentFile";

        this.mockMvc.perform(MockMvcRequestBuilders.get("/search")
                        .param("query", query)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("[]"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            " ",
            "     "
    })
    void itShouldReturn400StatusCodeWhenQueryIsBlank(String query) throws Exception {
        //given
        String expectedMessage = "query parameter is missing";

        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.get("/search")
                        .param("query", query)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(expectedMessage));

    }
}
