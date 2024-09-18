package danyatheworst.storage.directory;


import danyatheworst.storage.AbstractFileStorageIntegrationTests;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DirectoryRenamingIntegrationTests extends AbstractFileStorageIntegrationTests {

    @Test
    void itShouldRenameDirectoryAndReturn204StatusCode() throws Exception {
        this.minioRepository.createObject("user-1-files/directory_to_rename/");
        String path = "directory_to_rename";
        String newPath = "directory_to_rename_RENAMED";

        this.mockMvc.perform(MockMvcRequestBuilders.patch("/directories")
                        .param("path", path)
                        .param("newPath", newPath)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        String fullPath = this.pathComposer.composeDir(path, user.getId());
        String newFullPath = this.pathComposer.composeDir(newPath, user.getId());
        assertFalse(this.minioRepository.exists(fullPath));
        assertTrue(this.minioRepository.exists(newFullPath));
    }

    @Test
    void itShouldReturn204StatusCodeIfPathEqualsNewPathWhenRenamingDirectory() throws Exception {
        String path = "directory/directory_1";
        String newPath = "directory/directory_1";

        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.patch("/directories")
                        .param("path", path)
                        .param("newPath", newPath)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    void itShouldReturn409StatusCodeIfNewPathAlreadyExistsWhenRenamingDirectory() throws Exception {
        // Given: Creating two directories
        String path = "directory";
        String newPath = "directory_to_rename";
        this.minioRepository.createObject(this.pathComposer.composeDir(path, user.getId()));
        this.minioRepository.createObject(this.pathComposer.composeDir(newPath, user.getId()));

        String expectedMessage = newPath + " already exists";

        // When and then: Attempt to rename to an existing directory
        this.mockMvc.perform(MockMvcRequestBuilders.patch("/directories")
                        .param("path", path)
                        .param("newPath", newPath)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("message").value(expectedMessage));
    }

    @Test
    void itShouldReturn404StatusAndIfPathDoesNotExist() throws Exception {
        String path = "nonExistentDirectory";
        String newPath = "doesNotMatter";
        String expectedMessage = "No such directory: " + path;

        this.mockMvc.perform(MockMvcRequestBuilders.patch("/directories")
                        .param("path", path)
                        .param("newPath", newPath)
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("message").value(expectedMessage));
    }
}
