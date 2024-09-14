package danyatheworst.storage.compressing;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.io.InputStreamResource;

@AllArgsConstructor
@Getter
public class CompressedObject {
    private final String name;
    private final InputStreamResource inputStreamResource;
}
