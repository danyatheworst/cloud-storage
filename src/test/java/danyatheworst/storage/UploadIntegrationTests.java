package danyatheworst.storage;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UploadIntegrationTests extends FileStorageIntegrationTests {

    @Test
    void itShouldUploadObjectAndReturn201StatusCode() throws Exception {
        String path = "/";
        MockMultipartFile file1 = new MockMultipartFile("files",
                "folder/file1.txt",
                "text/plain",
                "someContent".getBytes()
        );

        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/upload")
                        .file(file1)
                        .param("path", path)
                        .with(authenticatedUser()))
                .andExpect(MockMvcResultMatchers.status().isCreated());

        String dirFullPath = this.pathComposer.composeDir("folder", this.user.getId());
        String fileFullPath = dirFullPath + "file1.txt";
        assertTrue(this.minioRepository.exists(dirFullPath));
        assertTrue(this.minioRepository.exists(fileFullPath));
    }
}
