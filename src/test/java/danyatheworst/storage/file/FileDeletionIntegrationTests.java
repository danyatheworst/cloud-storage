package danyatheworst.storage.file;

import danyatheworst.storage.FileStorageIntegrationTests;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class FileDeletionIntegrationTests extends FileStorageIntegrationTests {

    @Test
    void itShouldDeleteFileAndReturn200StatusCode() throws Exception {
        String path = "file_to_delete.txt";
        String fullPath = this.pathService.composeFile(path, user.getId());
        this.minioRepository.createObject(fullPath);

        this.mockMvc.perform(MockMvcRequestBuilders.delete("/files")
                        .param("path", path)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        assertFalse(this.minioRepository.exists(fullPath), path + " shouldn't be present in storage");
    }
}
