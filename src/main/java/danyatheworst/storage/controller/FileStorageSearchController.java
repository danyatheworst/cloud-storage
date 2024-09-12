package danyatheworst.storage.controller;

import danyatheworst.exceptions.InvalidParameterException;
import danyatheworst.storage.FileSystemObject;
import danyatheworst.storage.service.FileStorageSearchService;
import danyatheworst.user.User;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@Validated
@RestController
public class FileStorageSearchController {
    private final FileStorageSearchService fileStorageSearchService;

    @GetMapping("/search")
    public ResponseEntity<List<FileSystemObject>> search(
            @RequestParam @Size(max = 255) String query,
            @AuthenticationPrincipal User user
    ) {
        query = query.trim();
        if (query.isEmpty()) {
            throw new InvalidParameterException("query parameter is missing");
        }
        List<FileSystemObject> objects = this.fileStorageSearchService.search(query.trim(), user.getId());
        return ResponseEntity.status(HttpStatus.OK).body(objects);
    }
}
