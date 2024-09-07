package danyatheworst.storage;

import danyatheworst.exceptions.EntityAlreadyExistsException;
import danyatheworst.exceptions.EntityNotFoundException;
import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@AllArgsConstructor
@Service
public class FileStorageService {
    private final MinioRepository minioRepository;

    public void createDirectory(String path, Long userId) {
        //TEST

        int lastSlashIdx = path.lastIndexOf("/");
        if (lastSlashIdx != -1) {
            String parentDirectory = path.substring(0, lastSlashIdx);
            //race condition
            if (!this.directoryExists(parentDirectory)) {
                throw new EntityNotFoundException("No such directory: ".concat(parentDirectory));
            }
        }

        //TEST
        String directoryPath = this.composeDirectoryPath(path, userId);
        if (this.minioRepository.exists(directoryPath)) {
            throw new EntityAlreadyExistsException(directoryPath.concat(" already exists"));

        }

        this.minioRepository.createObject(directoryPath);
    }

    public boolean directoryExists(String path) {
        return this.minioRepository.exists(path);
    }

    private String composeDirectoryPath(String path, Long userId) {
        return "user-" + userId + "-files"
                .concat("/")
                .concat(path)
                .concat("/");
    }
}


