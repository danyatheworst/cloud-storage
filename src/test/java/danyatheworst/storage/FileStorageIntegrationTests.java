package danyatheworst.storage;

import danyatheworst.user.Role;
import danyatheworst.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


//TODO: create a parent test class with container etc and child test-classes for every case
// (creation, deletion, renaming, uploading)
// user-1-files/ helper func or smth

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class FileStorageIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    public MinioRepository minioRepository;

    private final User user = new User(1L, "username", "password", Set.of(Role.USER));

    @Container
    private static final GenericContainer<?> container =
            new GenericContainer<>(DockerImageName.parse("minio/minio"))
                    .withExposedPorts(9000)
                    .withEnv("MINIO_ROOT_USER", "minioadmin")
                    .withEnv("MINIO_ROOT_PASSWORD", "minioadmin")
                    .withCommand("server /data");

    @BeforeEach
    void setup() {
        this.minioRepository.createObject("user-1-files/");
    }

    @AfterEach
    void tearDown() {
        this.minioRepository
                .getContentRecursively("user-1-files/")
                .forEach(object -> this.minioRepository.removeObject(object.getPath()));
    }

    @Test
    void itShouldCreateDirectoryAndReturn201StatusCode() throws Exception {
        //given
        String path = "new-directory";

        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.post("/directories")
                        .param("path", path)
                        .with(SecurityMockMvcRequestPostProcessors.user(this.user))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers
                        .status()
                        .isCreated());

        String fullPath = "user-1-files/".concat(path).concat("/");
        boolean newDirectoryExists = this.minioRepository.exists(fullPath);

        assertTrue(newDirectoryExists, fullPath.concat(" should be present in storage"));
    }

    @Test
    public void itShouldReturn409StatusCodeWhenDirectoryAlreadyExists() throws Exception {
        //given
        String path = "new-directory";
        this.minioRepository.createObject("user-1-files/new-directory/");

        //when and then
        String expectedMessage = path.concat(" already exists");
        this.mockMvc.perform(MockMvcRequestBuilders.post("/directories")
                        .param("path", path)
                        .with(SecurityMockMvcRequestPostProcessors.user(this.user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status()
                        .isConflict())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$.message")
                        .value(expectedMessage)
                );
    }

    @Test
    void itShouldReturn404StatusCodeIfPathContainsNonExistentDirectory() throws Exception {
        //given
        String path = "nonExistentDirectory/new-directory";

        //when and then
        String expectedMessage = "No such directory: ".concat("nonExistentDirectory");
        this.mockMvc.perform(MockMvcRequestBuilders.post("/directories")
                        .param("path", path)
                        .with(SecurityMockMvcRequestPostProcessors.user(this.user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status()
                        .isNotFound())
                .andExpect(MockMvcResultMatchers
                        .jsonPath("$.message")
                        .value(expectedMessage)
                );
    }

    //TODO: a file deletion

    @Test
    void itShouldDeleteFileSystemObjectAndReturn200StatusCode() throws Exception {
        //given
        String path = "new-directory";
        this.minioRepository.createObject("user-1-files/new-directory/");

        //create some directories inside
        for (int i = 0; i < 10; i++) {
            this.minioRepository
                    .createObject("user-1-files/new-directory/".concat(i + "/"));
        }

        //when and then
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/objects")
                        .param("path", path)
                        .with(SecurityMockMvcRequestPostProcessors.user(this.user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status()
                        .isOk()
                );

        String fullPath = "user-1-files/".concat(path).concat("/");
        for (int i = 0; i < 20; i++) {
            String dirInside = fullPath.concat(i + "/");
            assertFalse(this.minioRepository.exists(dirInside));
        }

        boolean fileSystemObjectExists = this.minioRepository.exists(fullPath);
        assertFalse(fileSystemObjectExists, fullPath.concat(" shouldn't be present in storage"));
    }

    @Test
    void itShouldUploadObjectAndReturn201StatusCode() throws Exception {
        //given
        String path = "";

        MockMultipartFile file1 = new MockMultipartFile(
                "files",
                "folder/file1.txt",
                "text/plain",
                "someContent".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "files",
                "folder/file2.txt",
                "text/plain",
                "someContent".getBytes()
        );
        MockMultipartFile file3 = new MockMultipartFile(
                "files",
                "folder/folder_inside/file3.txt",
                "text/plain",
                "someContent".getBytes()
        );
        MockMultipartFile file4 = new MockMultipartFile(
                "files",
                "folder/folder_inside/file4.txt",
                "text/plain", "someContent".getBytes()
        );
        MockMultipartFile file5 = new MockMultipartFile(
                "files",
                "folder/folder_inside/one_more_folder/file5.txt",
                "text/plain",
                "someContent".getBytes()
        );

        // when and then
        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/objects/upload")
                        .file(file1)
                        .file(file2)
                        .file(file3)
                        .file(file4)
                        .file(file5)
                        .param("path", path)
                        .with(SecurityMockMvcRequestPostProcessors.user(this.user))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(MockMvcResultMatchers.status().isCreated());

        assertTrue(this.minioRepository.exists("user-1-files/folder/"));
        assertTrue(this.minioRepository.exists("user-1-files/folder/file1.txt"));
        assertTrue(this.minioRepository.exists("user-1-files/folder/file2.txt"));
        assertTrue(this.minioRepository.exists("user-1-files/folder/folder_inside/"));
        assertTrue(this.minioRepository.exists("user-1-files/folder/folder_inside/file3.txt"));
        assertTrue(this.minioRepository.exists("user-1-files/folder/folder_inside/file4.txt"));
        assertTrue(this.minioRepository.exists("user-1-files/folder/folder_inside/one_more_folder/"));
        assertTrue(this.minioRepository.exists("user-1-files/folder/folder_inside/one_more_folder/file5.txt"));
    }
}
