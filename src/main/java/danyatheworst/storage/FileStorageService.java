package danyatheworst.storage;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class FileStorageService {
    private final MinioRepository minioRepository;

    public void createDirectory(String path, String folderName, Long userId) {
        String emptyFolderName = this.directoryAbsolutePath(path, userId) + folderName + "/";
        this.minioRepository.createObject(emptyFolderName);
    }

    private String directoryAbsolutePath(String path, Long userId) {
        String basePath = "user-" + userId + "-files/";

        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        if (path.isEmpty()) {
            return basePath;
        }

        String dirFullPath = basePath + path;

        if (!dirFullPath.endsWith("/")) {
            dirFullPath += "/";
        }

        return dirFullPath;
    }
}


