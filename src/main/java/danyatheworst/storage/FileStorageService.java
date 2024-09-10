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

    public void createDirectory(String path, Long userId) {
        String directoryPath = this.composeObjectPath(path, userId).concat("/");
        this.minioRepository.createObject(directoryPath);
    }

    public void deleteObject(String path, Long userId) {
        path = this.composeObjectPath(path, userId);
        List<FileSystemObject> toDelete = this.minioRepository.getContentRecursively(path);

        toDelete.forEach(object -> this.minioRepository.removeObject(object.getPath()));
    }

    public void uploadFile(MultipartFile file, String path, Long userId) {
        this.minioRepository.uploadObject(file, this.composeObjectPath(path, userId));
    }

    public boolean directoryExists(String path, Long usedId) {
        return this.minioRepository.exists(this.composeObjectPath(path, usedId).concat("/"));
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

    private String composeObjectPath(String path, Long userId) {
        return "user-" + userId + "-files"
                .concat("/")
                .concat(path);
    }
}