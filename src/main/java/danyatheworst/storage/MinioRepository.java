package danyatheworst.storage;

import io.minio.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.io.ByteArrayInputStream;

@RequiredArgsConstructor
@Repository
public class MinioRepository {
    private final MinioClient client;
    private final String defaultBucketName = "user-files";

    public void createObject(String prefix) {
        try {

            this.client.putObject(
                    PutObjectArgs.builder()
                            .bucket(this.defaultBucketName)
                            .object(prefix)
                            .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                            .build()
            );
        } catch (Exception e) {
            throw new InternalError("Something went wrong during an empty folder creation");
        }
    }
}
