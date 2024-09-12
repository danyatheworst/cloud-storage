package danyatheworst.storage.controller;

import danyatheworst.storage.PathValidator;
import danyatheworst.storage.FileSystemObject;
import danyatheworst.storage.service.FileStorageService;
import danyatheworst.user.User;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@AllArgsConstructor
@Validated
@RestController
public class FileStorageController {
    private final FileStorageService fileStorageService;
    private final PathValidator pathValidator;

    @GetMapping("/directories")
    public ResponseEntity<List<FileSystemObject>> getContent(
            @RequestParam @Size(min = 1, max = 255) String path,
            @AuthenticationPrincipal User user
    ) {
        path = path.trim();
        this.pathValidator.validate(path);
        List<FileSystemObject> objects = this.fileStorageService.getContent(path, user.getId());
        return ResponseEntity.status(HttpStatus.OK).body(objects);
    }

    @PostMapping("/directories")
    public ResponseEntity<Void> createDirectory(
            @RequestParam @Size(min = 1, max = 255) String path,
            @AuthenticationPrincipal User user
    ) {
        path = path.trim();
        this.pathValidator.validate(path);
        this.fileStorageService.createDirectory(path, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/directories")
    public ResponseEntity<Void> deleteDirectory(
            @RequestParam @Size(min = 1, max = 255) String path,
            @AuthenticationPrincipal User user
    ) {
        path = path.trim();
        this.pathValidator.validate(path);
        this.fileStorageService.deleteDirectory(path, user.getId());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/files")
    public ResponseEntity<Void> deleteFiles(
            @RequestParam @Size(min = 1, max = 255) String path,
            @AuthenticationPrincipal User user
    ) {
        path = path.trim();
        this.pathValidator.validate(path);
        this.fileStorageService.deleteFile(path, user.getId());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PatchMapping("/directories")
    public ResponseEntity<Void> renameDirectory(
            @RequestParam @Size(min = 1, max = 255) String path,
            @RequestParam("newPath") @Size(min = 1, max = 255) String newPath,
            @AuthenticationPrincipal User user
    ) {
        this.pathValidator.validate(path);
        this.pathValidator.validate(newPath);
        if (!path.equals(newPath)) {
            this.fileStorageService.renameDirectory(path, newPath, user.getId());
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PatchMapping("/files")
    public ResponseEntity<Void> renameFile(
            @RequestParam @Size(min = 1, max = 255) String path,
            @RequestParam("newPath") @Size(min = 1, max = 255) String newPath,
            @AuthenticationPrincipal User user
    ) {
        path = path.trim();
        newPath = newPath.trim();
        this.pathValidator.validate(path);
        this.pathValidator.validate(newPath);

        if (!path.equals(newPath)) {
            this.fileStorageService.renameFile(path, newPath, user.getId());
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
