package danyatheworst.storage.compressing;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.InputStream;

@Getter
@AllArgsConstructor
public class ObjectBinary {
    @Setter
    private String path;
    private final String name;
    private final InputStream stream;
}
