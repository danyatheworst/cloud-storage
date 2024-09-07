package danyatheworst.storage;

import danyatheworst.exceptions.InvalidParameterException;
import danyatheworst.user.User;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


//directory — for directory-like objects that can contain other files or folders.
//content — folders and files inside a directory

//file — for regular files (e.g., presentation.pdf, notes.txt, archive.zip etc), which are data objects.

//FSO (file system object) — a generic term to refer to both files and folders

@AllArgsConstructor
@Validated
@RestController
public class FileStorageController {
    private final FileStorageService fileStorageService;

    @PostMapping("/directories")
    public ResponseEntity<Void> createDirectory(
            @RequestParam @Size(min = 1, max = 255) String path,
            @AuthenticationPrincipal User user
    ) {
        path = path.trim();
        pathValidation(path);

        this.fileStorageService.createDirectory(path, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    private void pathValidation(String path) {
        String[] segments = path.split("/");

        //path contains only "/" characters ("/", "//", "///", "/////////...////" etc)
        if (segments.length == 0 && !path.isEmpty()) {
            throw new InvalidParameterException("Path is invalid.");
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
                throw new InvalidParameterException("Path can't contain an escape sequence.");
            }
        }
    }

}
