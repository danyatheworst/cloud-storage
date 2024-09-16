package danyatheworst.storage;

import danyatheworst.exceptions.EntityNotFoundException;
import danyatheworst.exceptions.InternalServerException;
import danyatheworst.storage.compressing.ObjectBinary;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.ErrorResponse;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    public List<FileSystemObject> getContent(String prefix) {
        try {
            List<FileSystemObject> objects = new ArrayList<>();

            Iterable<Result<Item>> results = this.client.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(this.bucket)
                            .prefix(prefix)
                            .build()
            );

            for (Result<Item> result : results) {
                objects.add(convert(result.get()));
            }
            return objects;
        } catch (Exception e) {
            throw new InternalError("Something went wrong during getting content");
        }
    }

    public List<FileSystemObject> getContentRecursively(String prefix) {
        try {
            List<FileSystemObject> objects = new ArrayList<>();

            Iterable<Result<Item>> results = this.client.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(this.bucket)
                            .prefix(prefix)
                            .recursive(true)
                            .build()
            );

            for (Result<Item> result : results) {
                objects.add(convert(result.get()));
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

    public void uploadObject(MultipartFile file, String prefix) {
        try {
            InputStream inputStream = file.getInputStream();

            this.client.putObject(
                    PutObjectArgs.builder()
                            .bucket(this.bucket)
                            .object(prefix)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            inputStream.close();
        } catch (Exception e) {
            throw new InternalError("Something went wrong during an object uploading");
        }
    }

    public void copyObject(String fromPrefix, String toPrefix) {
        try {
            this.client.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(this.bucket)
                            .object(toPrefix)
                            .source(CopySource.builder()
                                    .bucket(this.bucket)
                                    .object(fromPrefix)
                                    .build())
                            .build());
        } catch (Exception e) {
            throw new InternalError("Something went wrong during an object copying");
        }
    }

    public ObjectBinary getObject(String prefix) {
        try {
            InputStream stream = this.client.getObject(
                    GetObjectArgs.builder()
                            .bucket(this.bucket)
                            .object(prefix)
                            .build());

            return new ObjectBinary(prefix, extractName(prefix), stream);
        } catch (Exception e) {
            if (e instanceof ErrorResponseException) {
                ErrorResponse errorResponse = ((ErrorResponseException) e).errorResponse();
                boolean doesNotExist = errorResponse.code().equals("NoSuchKey");
                if (doesNotExist) {
                    String name = extractName(errorResponse.objectName());
                    throw new EntityNotFoundException("No such file or directory: " + name);
                }
            }
            throw new InternalError("Something went wrong during a getting object inputStream");
        }
    }

    private static FileSystemObject convert(Item item) {
        String objectName = item.objectName();
        String name = extractName(objectName);
        boolean isDir = objectName.endsWith("/");
        return new FileSystemObject(objectName, name, isDir, localDateTime(item));
    }

    private static LocalDateTime localDateTime(Item item) {
        try {
            return item.lastModified().toLocalDateTime();
        } catch (NullPointerException e) {
            return null;
        }
    }

    private static String extractName(String path) {
        String[] segments = path.split("/");
        return segments[segments.length - 1];
    }
}
