package danyatheworst.storage.controller;

import danyatheworst.storage.PathValidator;
import danyatheworst.storage.compressing.CompressedObject;
import danyatheworst.storage.compressing.ObjectBinary;
import danyatheworst.storage.service.DownloadingService;
import danyatheworst.user.User;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@AllArgsConstructor
public class FileStorageDownloadController {
    private final PathValidator pathValidator;
    private final DownloadingService downloadingService;

    @GetMapping("/directories/download")
    public ResponseEntity<Resource> downloadDirectory(
            @RequestParam @Size(min = 1, max = 255) String path,
            @AuthenticationPrincipal User user
    ) throws Exception {
        this.pathValidator.validate(path);
        CompressedObject compressed = this.downloadingService.downloadDirectory(path, user.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + compressed.getName());

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(compressed.getInputStreamResource());
    }

    @GetMapping("/files/download")
    public ResponseEntity<Resource> downloadFile(
            @RequestParam @Size(min = 1, max = 255) String path,
            @AuthenticationPrincipal User user
    ) {
        this.pathValidator.validate(path);
        ObjectBinary objectBinary = this.downloadingService.downloadFile(path, user.getId());
        String fileName = objectBinary.getName();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(objectBinary.getStream()));
    }
}