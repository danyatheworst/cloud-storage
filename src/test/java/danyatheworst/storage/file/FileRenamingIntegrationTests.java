package danyatheworst.storage.file;

import danyatheworst.storage.FileStorageIntegrationTests;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileRenamingIntegrationTests extends FileStorageIntegrationTests {

    @Test
    void itShouldRenameFileAndReturn200StatusCode() throws Exception {
        // Create the original file

        String path = "file_to_rename.txt";
        String fullPath = this.pathComposer.composeFile(path, user.getId());
        String newPath = "file_to_rename_RENAMED.txt";
        String fullNewPath = this.pathComposer.composeFile(newPath, user.getId());

        this.minioRepository.createObject(fullPath);

        // Perform the rename
        this.mockMvc.perform(MockMvcRequestBuilders.patch("/files")
                        .param("path", path)
                        .param("newPath", newPath)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        // Check that the original file no longer exists and the new file exists
        assertFalse(this.minioRepository.exists(fullPath));
        assertTrue(this.minioRepository.exists(fullNewPath));
    }

    @Test
    void itShouldReturn409StatusCodeIfNewPathAlreadyExistsWhenRenamingFile() throws Exception {
        //given
        String path = "file.txt";
        String newPath = "file_to_rename.txt";
        this.minioRepository.createObject(this.pathComposer.composeFile(path, user.getId()));
        this.minioRepository.createObject(this.pathComposer.composeFile(newPath, user.getId()));
        String expectedMessage = newPath + " already exists";

        //when
        this.mockMvc.perform(MockMvcRequestBuilders.patch("/files")
                        .param("path", path)
                        .param("newPath", newPath)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("message").value(expectedMessage));
    }

    @Test
    void itShouldReturn200StatusCodeIfPathEqualsNewPathWhenRenamingFile() throws Exception {
        String path = "directory/file.txt";
        String newPath = "directory/file.txt";

        this.mockMvc.perform(MockMvcRequestBuilders.patch("/files")
                        .param("path", path)
                        .param("newPath", newPath)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void itShouldReturn404StatusCodeIfPathDoesNotExist() throws Exception {
        String path = "nonExistentFile.txt";
        String newPath = "does_not_matter_at_all";
        String expectedMessage = "No such file or directory: " + path;

        this.mockMvc.perform(MockMvcRequestBuilders.patch("/files")
                        .param("path", path)
                        .param("newPath", newPath)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value(expectedMessage));
    }
}
