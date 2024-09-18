package danyatheworst.storage.directory;

import danyatheworst.storage.AbstractFileStorageIntegrationTests;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class GetContentOfDirectoryIntegrationTests extends AbstractFileStorageIntegrationTests {
    @Test
    void itShouldReturn404StatusAndIfPathDoesNotExist() throws Exception {
        String path = "nonExistentDirectory";
        String expectedMessage = "No such directory: " + path;

        this.mockMvc.perform(MockMvcRequestBuilders.get("/directories")
                        .param("path", path)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("message").value(expectedMessage));
    }
}
