package danyatheworst.storage;

import danyatheworst.exceptions.InternalServerException;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class MinioRepository {
    private final MinioClient client;

    @Value("${minio.bucket}")
    private String bucket;

    public boolean exists(String prefix) {
        try {
            this.client.statObject(StatObjectArgs.builder()
                    .bucket(this.bucket)
                    .object(prefix)
                    .build()
            );

            return true;
        } catch (ErrorResponseException ex) {
            return false;
        } catch (Exception e) {
            throw new InternalServerException("Something went wrong");
        }
    }

    public void createObject(String prefix) {
        try {
            this.client.putObject(
                    PutObjectArgs.builder()
                            .bucket(this.bucket)
                            .object(prefix)
                            .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                            .build()
            );
        } catch (Exception e) {
            throw new InternalError("Something went wrong during an empty folder creation");
        }
    }

    public List<FileSystemObject> getContentRecursively(String prefix)  {
        try {
            List<FileSystemObject> objects = new ArrayList<>();

            Iterable<Result<Item>> results = this.client.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(this.bucket)
                            .prefix(prefix)
                            .recursive(true)  // Recursive listing
                            .build()
            );
            for (Result<Item> result : results) {
                objects.add(this.convert(result.get().objectName()));
            }
            return objects;
        } catch (Exception e) {
            throw new InternalError("Something went wrong during getting content");
        }
    }

    public void removeObject(String prefix) {
        try {
            this.client.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(this.bucket)
                            .object(prefix)
                            .build()
            );
        } catch (Exception e) {
            throw new InternalError("Something went wrong during removing object");

        }
    }

    private FileSystemObject convert(String objectName) {
        //TODO: add size?, lastModified
        String[] segments = objectName.split("/");
        String name = segments[segments.length - 1];
        boolean isDir = objectName.endsWith("/");
        return new FileSystemObject(objectName, name, isDir);
    }
}
