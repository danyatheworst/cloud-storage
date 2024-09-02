package danyatheworst.storage;

import danyatheworst.user.User;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


//directory — for directory-like objects that can contain other files or folders.
//content — folders and files inside a directory

//file — for regular files (e.g., presentation.pdf, notes.txt, archive.zip etc), which are data objects.

//FSO (file system object) — a generic term to refer to both files and folders

@AllArgsConstructor
@RestController
public class FileStorageController {
    private final FileStorageService fileStorageService;

    @PostMapping("/directories")
    public ResponseEntity<Void> createDirectory(
            @RequestParam(value = "path", defaultValue = "/") String path,
            @RequestBody @Valid RequestFSODto createDirectoryDto
    ) {
        this.fileStorageService.createDirectory(path.trim(), createDirectoryDto.getName(), this.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    private Long getUserId() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getId();
    }
}