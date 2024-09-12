package danyatheworst.storage;

import danyatheworst.storage.service.PathService;
import danyatheworst.user.Role;
import danyatheworst.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Set;

//TODO: when user is created "user-${userId}-files/" is created too

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class FileStorageIntegrationTests {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected MinioRepository minioRepository;

    @Autowired
    protected PathService pathService;

    protected final User user = new User(1L, "username", "password", Set.of(Role.USER));

    @Container
    private static final GenericContainer<?> container =
            new GenericContainer<>(DockerImageName.parse("minio/minio"))
                    .withExposedPorts(9000)
                    .withEnv("MINIO_ROOT_USER", "minioadmin")
                    .withEnv("MINIO_ROOT_PASSWORD", "minioadmin")
                    .withCommand("server /data");

    @BeforeEach
    void setup() {
        this.minioRepository.createObject(this.pathService.composeDir("/", user.getId()));
    }

    @AfterEach
    void tearDown() {
        this.minioRepository
                .getContentRecursively(this.pathService.composeDir("/", user.getId()))
                .forEach(object -> this.minioRepository.removeObject(object.getPath()));
    }

    protected RequestPostProcessor authenticatedUser() {
        return SecurityMockMvcRequestPostProcessors.user(this.user);
    }
}
