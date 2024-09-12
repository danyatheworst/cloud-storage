package danyatheworst.storage.service;

import danyatheworst.exceptions.EntityAlreadyExistsException;
import danyatheworst.exceptions.EntityNotFoundException;
import danyatheworst.storage.FileSystemObject;
import danyatheworst.storage.MinioRepository;
import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;


@AllArgsConstructor
@Service
public class FileStorageService {
    private final PathService pathService;
    private final MinioRepository minioRepository;

    public List<FileSystemObject> getContent(String path, Long userId) {
        String composeObjectPath = this.pathService.composeDir(path, userId);

        return this.minioRepository
                    .getContent(composeObjectPath)
                    .stream()
                    .filter(object -> !object.getPath().equals(composeObjectPath))
                    .map(object -> {
                        String pathRootRemoved = object.getPath().substring(composeObjectPath.length());
                        object.setPath(pathRootRemoved);
                        return object;
                    })
                    .toList();
    }

    public void createDirectory(String path, Long userId) {
        //race condition
        boolean dirExists = this.directoryExists(path, userId);
        if (dirExists) {
            throw new EntityAlreadyExistsException(path + " already exists");
        }
        this.parentExistenceValidation(path, userId);

        String directoryPath = this.pathService.composeDir(path, userId);
        this.minioRepository.createObject(directoryPath);
    }

    public void deleteDirectory(String path, Long userId) {
        //race condition
        this.directoryExists(path, userId);

        path = this.pathService.composeDir(path, userId);
        List<FileSystemObject> toDelete = this.minioRepository.getContentRecursively(path);

        toDelete.forEach(object -> this.minioRepository.removeObject(object.getPath()));
    }

    public void deleteFile(String path, Long userId) {
        //race condition
        this.fileExists(path, userId);
        String fullPath = this.pathService.composeFile(path, userId);
        this.minioRepository.removeObject(fullPath);
    }

    public void renameDirectory(String path, String newPath, Long userId) {
        //race condition
        if (!this.directoryExists(path, userId)) {
            throw new EntityNotFoundException("No such directory: ".concat(path));
        }

        this.parentExistenceValidation(newPath, userId);

        if (this.directoryExists(newPath, userId)) {
            throw new EntityAlreadyExistsException(newPath + " already exists");
        }

        String directoryPath = this.pathService.composeDir(path, userId);
        List<FileSystemObject> contentToMove = this.minioRepository.getContentRecursively(directoryPath);

        String newDirectoryPath = this.pathService.composeDir(newPath, userId);

        contentToMove
                .forEach(object -> {
                    String relativeObjectPath = object.getPath().substring(directoryPath.length());
                    String objectNewPath = newDirectoryPath.concat(relativeObjectPath);
                    this.minioRepository.copyObject(object.getPath(), objectNewPath);
                });
        this.deleteDirectory(path, userId);
    }

    public void renameFile(String path, String newPath, Long userId) {
        //race condition
        boolean fileExists = this.fileExists(path, userId);
        if (!fileExists) {
            throw new EntityNotFoundException("No such file: " + path);
        }
        this.parentExistenceValidation(newPath, userId);

        if (this.fileExists(newPath, userId)) {
            throw new EntityAlreadyExistsException(newPath + " already exists");
        }

        String fullPath = this.pathService.composeFile(path, userId);
        String newFullPath = this.pathService.composeFile(newPath, userId);
        this.minioRepository.copyObject(fullPath, newFullPath);
        this.deleteFile(path, userId);
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

    public boolean directoryExists(String path, Long usedId) {
        return this.minioRepository.exists(this.pathService.composeDir(path, usedId));
    }

    public boolean fileExists(String path, Long usedId) {
        return this.minioRepository.exists(this.pathService.composeFile(path, usedId));
    }


}