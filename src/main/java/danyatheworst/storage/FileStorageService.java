package danyatheworst.storage;

import danyatheworst.exceptions.EntityAlreadyExistsException;
import danyatheworst.exceptions.EntityNotFoundException;
import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;


@AllArgsConstructor
@Service
public class FileStorageService {
    private final MinioRepository minioRepository;

    public void createDirectory(String path, Long userId) {
        int lastSlashIdx = path.lastIndexOf("/");
        if (lastSlashIdx != -1) {
            String parentDirectory = path.substring(0, lastSlashIdx);
            //race condition
            if (!this.directoryExists(parentDirectory)) {
                throw new EntityNotFoundException("No such directory: ".concat(parentDirectory));
            }
        }

        String directoryPath = this.composeDirectoryPath(path, userId).concat("/");
        if (this.minioRepository.exists(directoryPath)) {
            throw new EntityAlreadyExistsException(directoryPath.concat(" already exists"));

        }

        this.minioRepository.createObject(directoryPath);
    }

    public boolean directoryExists(String path) {
        return this.minioRepository.exists(path);
    }

    public void deleteObject(String path, Long userId) {
        path = this.composeDirectoryPath(path, userId);
        List<FileSystemObject> toDelete = this.minioRepository.getContentRecursively(path);

        toDelete.forEach(object -> this.minioRepository.removeObject(object.getPath()));
    }

    private String composeDirectoryPath(String path, Long userId) {
        return "user-" + userId + "-files"
                .concat("/")
                .concat(path);
    }
}