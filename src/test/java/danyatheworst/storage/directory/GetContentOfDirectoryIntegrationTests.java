package danyatheworst.storage.directory;

import danyatheworst.storage.FileStorageIntegrationTests;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class GetContentOfDirectoryIntegrationTests extends FileStorageIntegrationTests {
    @Test
    void itShouldReturn200StatusAndEmptyListIfPathDoesNotExist() throws Exception {
        String path = "nonExistentDirectory";

        this.mockMvc.perform(MockMvcRequestBuilders.get("/directories")
                        .param("path", path)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("[]"));
    }
}
