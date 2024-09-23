package danyatheworst.storage;

import danyatheworst.auth.RegistrationService;
import danyatheworst.auth.RequestSignUpDto;
import danyatheworst.config.minio.MinioClientConfiguration;
import danyatheworst.storage.service.FileStorageService;
import danyatheworst.user.User;
import danyatheworst.user.UserRepository;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {
        RegistrationService.class,
        MinioRepository.class,
        MinioClientConfiguration.class,
        FileStorageService.class,
        PathComposer.class
})
public class RootDirectoryCreationIntegrationTests {
    private static final String MINIO_USER = "testuser-rootdir";
    private static final String MINIO_PASSWORD = "testuser-rootdir";
    private static final String MINIO_BUCKET = "user-files-test-rootdir";

    private static final MinIOContainer minio = new MinIOContainer("minio/minio")
            .withUserName(MINIO_USER)
            .withPassword(MINIO_PASSWORD);

    static {
        minio.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("minio.url", minio::getS3URL);
        registry.add("minio.username", minio::getUserName);
        registry.add("minio.password", minio::getPassword);
        registry.add("minio.bucket", () -> MINIO_BUCKET);
    }

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

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private MinioRepository minioRepository;

    @AfterEach
    void tearDown() {
        this.minioRepository
                .getContentRecursively(("user-1-files/"))
                .forEach(object -> this.minioRepository.removeObject(object.getPath()));
    }

    @Test
    public void itShouldCreateUserRootDirectoryAfterSigningUp() {
        createBucket();
        //given
        Long userId = 1L;
        String login = "user";
        String rawPassword = "user";
        RequestSignUpDto signUpDto = new RequestSignUpDto(login, rawPassword);

        when(this.passwordEncoder.encode(rawPassword)).thenReturn("encodedPass");

        when(this.userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(userId);
            return user;
        });

        // When
        this.registrationService.register(signUpDto);

        //then
        String directoryPath = "user-" + userId + "-files/";
        boolean directoryExists = this.minioRepository.exists(directoryPath);
        assertTrue(directoryExists, "User root directory 'user-1-files' should exist after registration.");
    }
}
