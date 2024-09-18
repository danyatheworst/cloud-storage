package danyatheworst.storage.directory;

import danyatheworst.storage.AbstractFileStorageIntegrationTests;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DirectoryCreationIntegrationTests extends AbstractFileStorageIntegrationTests {

    @Test
    void itShouldCreateDirectoryAndReturn201StatusCode() throws Exception {
        String path = "new-directory";

        this.mockMvc.perform(MockMvcRequestBuilders.post("/directories")
                        .param("path", path)
                        .with(this.authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated());

        String fullPath = this.pathComposer.composeDir(path, user.getId());
        boolean newDirectoryExists = this.minioRepository.exists(fullPath);

        assertTrue(newDirectoryExists, fullPath.concat(" should be present in storage"));
    }

    @Test
    void itShouldReturn404StatusCodeIfPathContainsNonExistentDirectory() throws Exception {
        String path = "nonExistentDirectory/new-directory";
        String expectedMessage = "No such directory: " + "nonExistentDirectory";

        this.mockMvc.perform(MockMvcRequestBuilders.post("/directories")
                        .param("path", path)
                        .with(this.authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(expectedMessage));
    }

    @Test
    void itShouldReturn409StatusCodeWhenDirectoryAlreadyExists() throws Exception {
        String path = "new-directory";
        this.minioRepository.createObject(pathComposer.composeDir(path, user.getId()));

        String expectedMessage = path.concat(" already exists");
        this.mockMvc.perform(MockMvcRequestBuilders.post("/directories")
                        .param("path", path)
                        .with(SecurityMockMvcRequestPostProcessors.user(this.user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(expectedMessage));
    }
}
