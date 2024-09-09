package danyatheworst.storage;

import danyatheworst.user.Role;
import danyatheworst.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
        String expectedMessage = "user-1-files/"
                .concat(path)
                .concat("/")
                .concat(" already exists");
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
}
