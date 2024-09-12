package danyatheworst.storage.controller;

import danyatheworst.common.ErrorResponseDto;
import danyatheworst.exceptions.InvalidParameterException;
import danyatheworst.storage.PathValidator;
import danyatheworst.storage.service.FileStorageUploadService;
import danyatheworst.user.User;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@AllArgsConstructor
@RestController
public class FileStorageUploadController {
    private final PathValidator pathValidator;
    private final FileStorageUploadService fileStorageUploadService;


    @PostMapping("/upload")
    public ResponseEntity<Void> uploadObject(
            @RequestParam String path,
            @RequestBody @NotNull List<MultipartFile> files,
            @AuthenticationPrincipal User user
    ) {
        path = path.trim();
        this.pathValidator.validate(path);

        //When a client sends an empty file field
        //the files list contains one element where the filename is an empty string ("").
        if (files.size() == 1 && files.get(0).getOriginalFilename().isEmpty()) {
            throw new InvalidParameterException("files cannot be empty");
        }

        files.forEach(file -> {
                    this.pathValidator.validate(file.getOriginalFilename());
                }
        );

        this.fileStorageUploadService.upload(files, path, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponseDto> handleMaxSizeException(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(
                new ErrorResponseDto("File size exceeds the maximum allowed limit 400MB."));
    }
}
