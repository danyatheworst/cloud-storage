package danyatheworst.storage.directory;

import danyatheworst.storage.FileStorageIntegrationTests;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class DirectoryDeletionIntegrationTests extends FileStorageIntegrationTests {

    @Test
    void itShouldDeleteDirectoryAndReturn204StatusCode() throws Exception {
        String path = "new-directory";
        this.minioRepository.createObject(this.pathComposer.composeDir(path, user.getId()));

        // Create some nested directories
        for (int i = 0; i < 10; i++) {
            String nestedPath = path + "/" + i;
            String nestedDir = this.pathComposer.composeDir(nestedPath, user.getId());
            this.minioRepository.createObject(nestedDir);
        }

        this.mockMvc.perform(MockMvcRequestBuilders.delete("/directories")
                        .param("path", path)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        String fullPath = this.pathComposer.composeDir(path, user.getId());
        for (int i = 0; i < 10; i++) {
            assertFalse(this.minioRepository.exists(fullPath.concat(i + "/")));
        }

        assertFalse(this.minioRepository.exists(fullPath), fullPath.concat(" shouldn't be present in storage"));
    }

    @Test
    void itShouldReturn404StatusAndIfPathDoesNotExist() throws Exception {
        String path = "nonExistentDirectory";
        String expectedMessage = "No such directory: " + path;

        this.mockMvc.perform(MockMvcRequestBuilders.delete("/directories")
                        .param("path", path)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("message").value(expectedMessage));
    }
}
