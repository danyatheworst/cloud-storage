package danyatheworst.config.minio;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component

public class MinioBucketConfiguration {
    @Value("${minio.bucket}")
    private String bucketName;

    private final MinioClient minioClient;

    public MinioBucketConfiguration(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeBucket() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(this.bucketName).build());
            if (!exists) {
                this.minioClient.makeBucket(MakeBucketArgs.builder().bucket(this.bucketName).build());
            }
        } catch (Exception e) {
            System.err.println("Error occurred while checking/creating bucket: " + e.getMessage());
        }
    }
}
