package danyatheworst.storage.service;

import danyatheworst.exceptions.EntityAlreadyExistsException;
import danyatheworst.exceptions.EntityNotFoundException;
import danyatheworst.storage.FileSystemObject;
import danyatheworst.storage.MinioRepository;
import danyatheworst.storage.PathComposer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@AllArgsConstructor
@Service
public class FileStorageService {
    private final PathComposer pathComposer;
    private final MinioRepository minioRepository;

    public List<FileSystemObject> getContent(String path, Long userId) {
        String composedObjectPath = this.pathComposer.composeDir(path, userId);

        boolean exists = this.minioRepository.exists(composedObjectPath);
        if (!exists) {
            throw new EntityNotFoundException("No such directory: " + path);
        }

        return this.minioRepository
                    .getContent(composedObjectPath)
                    .stream()
                    .filter(object -> !object.getPath().equals(composedObjectPath))
                    .map(object -> {
                        object.setPath(this.pathComposer.removeRoot(object.getPath(), userId));
                        return object;
                    })
                    .toList();
    }

    public void createDirectory(String path, Long userId) {
        boolean dirExists = this.directoryExists(path, userId);
        if (dirExists) {
            throw new EntityAlreadyExistsException(path + " already exists");
        }
        this.parentExistenceValidation(path, userId);

        String directoryPath = this.pathComposer.composeDir(path, userId);
        this.minioRepository.createObject(directoryPath);
    }

    public void deleteDirectory(String path, Long userId) {
        String composedObjectPath = this.pathComposer.composeDir(path, userId);
        List<FileSystemObject> toDelete = this.minioRepository.getContentRecursively(composedObjectPath);

        if (toDelete.isEmpty()) {
            throw new EntityNotFoundException("No such directory: " + path);
        }

        toDelete.forEach(object -> this.minioRepository.removeObject(object.getPath()));
    }

    public void deleteFile(String path, Long userId) {
        String composedObjectPath = this.pathComposer.composeFile(path, userId);

        boolean exists = this.minioRepository.exists(composedObjectPath);
        if (!exists) {
            throw new EntityNotFoundException("No such file: " + path);
        }

        String fullPath = this.pathComposer.composeFile(path, userId);
        this.minioRepository.removeObject(fullPath);
    }

    public void renameDirectory(String path, String newPath, Long userId) {
        if (this.directoryExists(newPath, userId)) {
            throw new EntityAlreadyExistsException(newPath + " already exists");
        }

        String directoryPath = this.pathComposer.composeDir(path, userId);
        List<FileSystemObject> contentToMove = this.minioRepository.getContentRecursively(directoryPath);
        if (contentToMove.isEmpty()) {
            throw new EntityNotFoundException("No such directory: " + path);
        }

        String newDirectoryPath = this.pathComposer.composeDir(newPath, userId);

        contentToMove
                .forEach(object -> {
                    String relativeObjectPath = object.getPath().substring(directoryPath.length());
                    String objectNewPath = newDirectoryPath.concat(relativeObjectPath);
                    this.minioRepository.copyObject(object.getPath(), objectNewPath);
                });
        this.deleteDirectory(path, userId);
    }

    public void renameFile(String path, String newPath, Long userId) {
        String composedObjectPath = this.pathComposer.composeFile(path, userId);
        if (!this.minioRepository.exists(composedObjectPath)) {
            throw new EntityNotFoundException("No such file: " + path);
        }

        this.parentExistenceValidation(newPath, userId);

        if (this.fileExists(newPath, userId)) {
            throw new EntityAlreadyExistsException(newPath + " already exists");
        }

        String newFullPath = this.pathComposer.composeFile(newPath, userId);
        this.minioRepository.copyObject(composedObjectPath, newFullPath);
        this.deleteFile(path, userId);
    }

    public void parentExistenceValidation(String path, Long userId) {
        int lastSlashIdx = path.lastIndexOf("/");
        if (lastSlashIdx != -1) {
            String parentDirectory = path.substring(0, lastSlashIdx);

            if (!this.directoryExists(parentDirectory, userId)) {
                throw new EntityNotFoundException("No such directory: " + parentDirectory);
            }
        }
    }

    public boolean directoryExists(String path, Long usedId) {
        return this.minioRepository.exists(this.pathComposer.composeDir(path, usedId));
    }

    public boolean fileExists(String path, Long usedId) {
        return this.minioRepository.exists(this.pathComposer.composeFile(path, usedId));
    }
}