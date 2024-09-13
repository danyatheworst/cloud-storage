package danyatheworst.storage;

import danyatheworst.auth.RegistrationService;
import danyatheworst.auth.RequestSignUpDto;
import danyatheworst.user.User;
import danyatheworst.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ActiveProfiles("test")
@Testcontainers
@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class RootDirectoryCreationIntegrationTests {

    @Container
    private static final GenericContainer<?> container =
            new GenericContainer<>(DockerImageName.parse("minio/minio"))
                    .withExposedPorts(9000)
                    .withEnv("MINIO_ROOT_USER", "minioadmin")
                    .withEnv("MINIO_ROOT_PASSWORD", "minioadmin")
                    .withCommand("server /data");

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RegistrationService registrationService;  // Inject mocks into the RegistrationService

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
