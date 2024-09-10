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
                            object.setPath(object.getPath().substring(objectPath.length()));
                            return object;
                        })
                        .toList();
    }
}
