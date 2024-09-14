package danyatheworst.storage.service;

import danyatheworst.exceptions.EntityNotFoundException;
import danyatheworst.storage.FileSystemObject;
import danyatheworst.storage.MinioRepository;
import danyatheworst.storage.PathComposer;
import danyatheworst.storage.compressing.CompressedObject;
import danyatheworst.storage.compressing.Compressor;
import danyatheworst.storage.compressing.ObjectBinary;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@AllArgsConstructor
public class DownloadingService {
    private final MinioRepository minioRepository;
    private final PathComposer pathComposer;
    private final Compressor zipCompressor;

    public CompressedObject downloadDirectory(String path, Long userId) throws IOException {
        List<FileSystemObject> contentToDownload = this.minioRepository
                .getContentRecursively(this.pathComposer.composeDir(path, userId));
        if (contentToDownload.isEmpty()) {
            throw new EntityNotFoundException("No such file or directory: " + path);
        }

        List<ObjectBinary> objects = contentToDownload
                .stream()
                .map(object -> {
                    ObjectBinary objectBinary = this.minioRepository.getObject(object.getPath());
                    objectBinary.setPath(this.pathComposer.removeRoot(objectBinary.getPath(), userId));
                    return objectBinary;
                })
                .toList();
        return this.zipCompressor.compress(objects, "archive");
    }

    public ObjectBinary downloadFile(String path, Long userId) {
        return this.minioRepository.getObject(this.pathComposer.composeFile(path, userId));
    }
}
