package danyatheworst.storage.compressing;

import java.io.IOException;
import java.util.List;

public interface Compressor {
    CompressedObject compress(List<ObjectBinary> objects, String name) throws IOException;
}
