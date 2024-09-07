package danyatheworst.storage;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FileSystemObject {
    private final String path;
    private final String name;
    private final boolean isDir;
}
