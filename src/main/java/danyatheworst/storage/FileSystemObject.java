package danyatheworst.storage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class FileSystemObject {
    @Setter
    private String path;
    private final String name;
    private final boolean isDir;
}
