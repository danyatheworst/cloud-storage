package danyatheworst.storage;

import danyatheworst.exceptions.EntityNotFoundException;
import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@AllArgsConstructor
@Service
public class FileStorageService {
    private final MinioRepository minioRepository;

    //TODO: directoryExists?? directoryExistenceValidation?

    public void createDirectory(String path, Long userId) {
        String directoryPath = this.composeObjectPath(path, userId).concat("/");
        this.minioRepository.createObject(directoryPath);
    }

    public void deleteDirectory(String path, Long userId) {
        path = this.composeObjectPath(path, userId).concat("/");
        List<FileSystemObject> toDelete = this.minioRepository.getContentRecursively(path);

        toDelete.forEach(object -> this.minioRepository.removeObject(object.getPath()));
    }

    public void deleteFile(String path, Long userId) {
        String fullPath = this.composeObjectPath(path, userId);
        this.minioRepository.removeObject(fullPath);
    }

    public void uploadFile(MultipartFile file, String path, Long userId) {
        this.minioRepository.uploadObject(file, this.composeObjectPath(path, userId));
    }

    public boolean directoryExists(String path, Long usedId) {
        return this.minioRepository.exists(this.composeObjectPath(path, usedId).concat("/"));
    }

    public boolean fileExists(String path, Long usedId) {
        return this.minioRepository.exists(this.composeObjectPath(path, usedId));
    }

    public void parentExistenceValidation(String path, Long userId) {
        int lastSlashIdx = path.lastIndexOf("/");
        if (lastSlashIdx != -1) {
            String parentDirectory = path.substring(0, lastSlashIdx);
            //race condition
            if (!this.directoryExists(parentDirectory, userId)) {
                throw new EntityNotFoundException("No such directory: ".concat(parentDirectory));
            }
        }
    }

    public void renameDirectory(String path, String newPath, Long userId) {
        String directoryPath = this.composeObjectPath(path, userId).concat("/");
        List<FileSystemObject> contentToMove = this.minioRepository.getContentRecursively(directoryPath);

        String newDirectoryPath = this.composeObjectPath(newPath, userId).concat("/");

        contentToMove
                .forEach(object -> {
                    String relativeObjectPath = object.getPath().substring(directoryPath.length());
                    String objectNewPath = newDirectoryPath.concat(relativeObjectPath);
                    this.minioRepository.copyObject(object.getPath(), objectNewPath);
                });
        this.deleteDirectory(path, userId);
    }

    public void renameFile(String path, String newPath, Long userId) {
        String fullPath = this.composeObjectPath(path, userId);
        String newFullPath = this.composeObjectPath(newPath, userId);
        this.minioRepository.copyObject(fullPath, newFullPath);
        this.deleteFile(path, userId);
    }

    private String composeObjectPath(String path, Long userId) {
        return "user-" + userId + "-files"
                .concat("/")
                .concat(path);
    }
}