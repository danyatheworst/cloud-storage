package danyatheworst.storage.file;

import danyatheworst.storage.AbstractFileStorageIntegrationTests;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class FileDeletionIntegrationTests extends AbstractFileStorageIntegrationTests {

    @Test
    void itShouldDeleteFileAndReturn204StatusCode() throws Exception {
        String path = "file_to_delete.txt";
        String fullPath = this.pathComposer.composeFile(path, user.getId());
        this.minioRepository.createObject(fullPath);

        this.mockMvc.perform(MockMvcRequestBuilders.delete("/files")
                        .param("path", path)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        assertFalse(this.minioRepository.exists(fullPath), path + " shouldn't be present in storage");
    }

    @Test
    void itShouldReturn404StatusAndIfPathDoesNotExist() throws Exception {
        String path = "nonExistentFile.txt";
        String expectedMessage = "No such file: " + path;

        this.mockMvc.perform(MockMvcRequestBuilders.delete("/files")
                        .param("path", path)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("message").value(expectedMessage));
    }
}
