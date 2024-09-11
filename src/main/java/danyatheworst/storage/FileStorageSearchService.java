package danyatheworst.storage;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class FileStorageSearchService {
    private final MinioRepository minioRepository;

    public List<FileSystemObject> search(String name, Long userId) {
        String objectPath = "user-" + userId + "-files/";
                return this.minioRepository
                        .getContentRecursively(objectPath)
                        .stream()
                        .filter(object -> object.getName().contains(name))
                        .map(object -> {
                            String pathRootRemoved = object.getPath().substring(objectPath.length());
                            if (pathRootRemoved.endsWith("/")) {
                                pathRootRemoved = pathRootRemoved.substring(0, pathRootRemoved.length() - 1);
                            }
                            object.setPath(pathRootRemoved);
                            return object;
                        })
                        .toList();
    }
}
