package danyatheworst.storage;

import danyatheworst.user.Role;
import danyatheworst.user.User;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Set;


@SpringBootTest
@AutoConfigureMockMvc
public abstract class AbstractFileStorageIntegrationTests {
    private static final String MINIO_USER = "testuser";
    private static final String MINIO_PASSWORD = "testpassword";
    private static final String MINIO_BUCKET = "user-files-test";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected MinioRepository minioRepository;

    @Autowired
    protected PathComposer pathComposer;

    protected final User user = new User(1L, "username", "password", Set.of(Role.USER));

    private static final MinIOContainer minio = new MinIOContainer("minio/minio")
            .withUserName(MINIO_USER)
            .withPassword(MINIO_PASSWORD);

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("minio.url", minio::getS3URL);
        registry.add("minio.username", minio::getUserName);
        registry.add("minio.password", minio::getPassword);
        registry.add("minio.bucket", () -> MINIO_BUCKET);

        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    static {
        minio.start();
        postgres.start();
//        redis.start();

    }

    @BeforeAll
    static void createBucket() {
        try {
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(minio.getS3URL())
                    .credentials(MINIO_USER, MINIO_PASSWORD)
                    .build();

            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(MINIO_BUCKET).build());

            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(MINIO_BUCKET).build());
            }
        } catch (Exception e) {
            System.err.println("Error occurred: " + e);
        }
    }

    @BeforeEach
    void setup() {
        this.minioRepository.createObject(this.pathComposer.composeDir("/", this.user.getId()));
    }

    @AfterEach
    void tearDown() {
        this.minioRepository
                .getContentRecursively(this.pathComposer.composeDir("/", this.user.getId()))
                .forEach(object -> this.minioRepository.removeObject(object.getPath()));
    }

    protected RequestPostProcessor authenticatedUser() {
        return SecurityMockMvcRequestPostProcessors.user(this.user);
    }
}
