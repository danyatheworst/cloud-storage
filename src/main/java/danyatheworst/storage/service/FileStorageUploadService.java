package danyatheworst.storage.service;

import danyatheworst.exceptions.EntityNotFoundException;
import danyatheworst.storage.MinioRepository;
import danyatheworst.storage.PathComposer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.StringJoiner;

@AllArgsConstructor
@Service
public class FileStorageUploadService {
    private final PathComposer pathComposer;
    private final MinioRepository minioRepository;

    public void upload(List<MultipartFile> files, String path, Long userId) {
        boolean dirExists = this.minioRepository.exists(this.pathComposer.composeDir(path, userId));
        if (!dirExists) {
            throw new EntityNotFoundException("No such directory: ".concat(path));
        }

        for (MultipartFile multipartFile : files) {
            String filePath = multipartFile.getOriginalFilename();
            this.handleNestedDirectories(filePath, userId);
            String fileToUpload = path.endsWith("/") ? path + filePath : path.concat("/") + filePath;
            this.uploadFile(multipartFile, fileToUpload, userId);
        }
    }

    private void uploadFile(MultipartFile file, String path, Long userId) {
        this.minioRepository.uploadObject(file, this.pathComposer.composeFile(path, userId));
    }

    private void handleNestedDirectories(String filePath, Long userId) {
        int lastSlashIndex =  filePath.lastIndexOf("/");
        if (lastSlashIndex == -1) {
            return;
        }
        String[] segments = filePath
                .substring(0, lastSlashIndex) //get only directories
                .split("/");
        StringJoiner stringJoiner = new StringJoiner("/");
        for (String segment : segments) {
            stringJoiner.add(segment);

            String directoryPath = this.pathComposer.composeDir(stringJoiner.toString(), userId);
            this.minioRepository.createObject(directoryPath);
        }
    }
}
