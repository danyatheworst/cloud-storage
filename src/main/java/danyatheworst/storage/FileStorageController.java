package danyatheworst.storage;

import danyatheworst.common.ErrorResponseDto;
import danyatheworst.exceptions.EntityAlreadyExistsException;
import danyatheworst.exceptions.EntityNotFoundException;
import danyatheworst.exceptions.InvalidParameterException;
import danyatheworst.user.User;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@AllArgsConstructor
@Validated
@RestController
public class FileStorageController {
    private final FileStorageService fileStorageService;
    private final FileStorageSearchService fileStorageSearchService;

    @GetMapping("/directories")
    public ResponseEntity<List<FileSystemObject>> getContent(
            @RequestParam @Size(max = 255) String path,
            @AuthenticationPrincipal User user
    ) {
        path = path.trim();
        if (!path.isEmpty()) {
            fileNameValidation(path);
        }
        List<FileSystemObject> objects = this.fileStorageService.getContent(path, user.getId());
        return ResponseEntity.status(HttpStatus.OK).body(objects);
    }

    @PostMapping("/directories")
    public ResponseEntity<Void> createDirectory(
            @RequestParam @Size(max = 255) String path,
            @AuthenticationPrincipal User user
    ) {
        path = path.trim();
        fileNameValidation(path);
        boolean dirExists = this.fileStorageService.directoryExists(path, user.getId());
        if (dirExists) {
            throw new EntityAlreadyExistsException(path.concat(" already exists"));
        }
        this.fileStorageService.parentExistenceValidation(path, user.getId());
        this.fileStorageService.createDirectory(path, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/directories")
    public ResponseEntity<Void> deleteDirectory(
            @RequestParam @Size(min = 1, max = 255) String path,
            @AuthenticationPrincipal User user
    ) {
        path = path.trim();
        fileNameValidation(path);
        this.fileStorageService.directoryExists(path, user.getId());

        this.fileStorageService.deleteDirectory(path, user.getId());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/files")
    public ResponseEntity<Void> deleteFiles(
            @RequestParam @Size(min = 1, max = 255) String path,
            @AuthenticationPrincipal User user
    ) {
        path = path.trim();
        fileNameValidation(path);
        this.fileStorageService.fileExists(path, user.getId());

        this.fileStorageService.deleteFile(path, user.getId());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/upload")
    public ResponseEntity<Void> uploadObject(
            @RequestParam @Size(max = 255) String path,
            @RequestBody @NotNull List<MultipartFile> files,
            @AuthenticationPrincipal User user
    ) {
        //When a client sends an empty file field
        //the files list contains one element where the filename is an empty string ("").
        if (files.size() == 1 && files.get(0).getOriginalFilename().isEmpty()) {
            throw new InvalidParameterException("files cannot be empty");
        }

        path = path.trim();
        if (!path.isEmpty()) {
            fileNameValidation(path);
        }

        this.fileStorageService.parentExistenceValidation(path, user.getId());

        for (MultipartFile multipartFile : files) {
            String filePath = multipartFile.getOriginalFilename();
            this.handleNestedDirectories(filePath, user.getId());
            this.fileStorageService.uploadFile(multipartFile, path.concat("/") + filePath, user.getId());
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/directories")
    public ResponseEntity<Void> renameDirectory(
            @RequestParam @Size(min = 1, max = 255) String path,
            @RequestParam("newPath") @Size(min = 1, max = 255) String newPath,
            @AuthenticationPrincipal User user
    ) {
        path = path.trim();
        newPath = newPath.trim();
        fileNameValidation(path);
        fileNameValidation(newPath);

        boolean dirExists = this.fileStorageService.directoryExists(path, user.getId());
        if (!dirExists) {
            throw new EntityNotFoundException("No such directory: ".concat(path));

        }
        this.fileStorageService.parentExistenceValidation(newPath, user.getId());
        this.fileStorageService.renameDirectory(path, newPath, user.getId());

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
        fileNameValidation(path);
        fileNameValidation(newPath);

        boolean fileExists = this.fileStorageService.fileExists(path, user.getId());
        if (!fileExists) {
            throw new EntityNotFoundException("No such file: ".concat(path));

        }
        this.fileStorageService.parentExistenceValidation(newPath, user.getId());
        this.fileStorageService.renameFile(path, newPath, user.getId());

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<FileSystemObject>> search(
            @RequestParam @Size(min = 1, max = 255) String name,
            @AuthenticationPrincipal User user
    ) {
        name = name.trim();
        fileNameValidation(name);
        List<FileSystemObject> objects = this.fileStorageSearchService.search(name, user.getId());
        return ResponseEntity.status(HttpStatus.OK).body(objects);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponseDto> handleMaxSizeException(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(
                new ErrorResponseDto("File size exceeds the maximum allowed limit 400MB."));
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
            this.fileStorageService.createDirectory(stringJoiner.toString(), userId);
        }
    }

    private void fileNameValidation(String path) {
        String[] segments = path.split("/");

        //path contains only "/" characters ("/", "//", "///", "/////////...////" etc)
        if (segments.length == 0 && !path.isEmpty()) {
            throw new InvalidParameterException("File name is invalid.");
        }

        for (String segment : segments) {
            if (segment.startsWith(".")) {
                throw new InvalidParameterException("File name can't start with a dot.");
            }
            if (segment.contains(":")) {
                throw new InvalidParameterException("File name can't contain a colon.");
            }
            if (segment.isEmpty()) {
                throw new InvalidParameterException("File name can't contain a slash.");
            }
            if (segment.contains("\\")) {
                throw new InvalidParameterException("File name can't contain a backslash.");
            }

            String regex = "[\n\t\f\b\r]";
            Matcher escapeSequenceMatcher = Pattern.compile(regex).matcher(segment);

            if (escapeSequenceMatcher.find()) {
                throw new InvalidParameterException("File name can't contain an escape sequence.");
            }
        }
    }
}
