package danyatheworst.storage;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class DownloadIntegrationTests extends AbstractFileStorageIntegrationTests {

    @Test
    void itShouldReturn404StatusCodeIfDirectoryToDownloadDoesNotExist() throws Exception {
        //given
        String path = "nonExistentDirectory";
        String expectedMessage = "No such file or directory: " + path;

        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.get("/directories/download")
                        .param("path", path)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("message").value(expectedMessage));
    }

    @Test
    void itShouldReturn404StatusCodeIfFileToDownloadDoesNotExist() throws Exception {
        //given
        String path = "nonExistentFile.png";
        String expectedMessage = "No such file or directory: " + path;

        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.get("/directories/download")
                        .param("path", path)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("message").value(expectedMessage));
    }
}
