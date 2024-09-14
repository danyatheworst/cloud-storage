package danyatheworst.storage.compressing;

import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class ZipCompressor implements Compressor {
    @Override
    public CompressedObject compress(List<ObjectBinary> objects, String name) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(byteArrayOutputStream);

        for (ObjectBinary objectBinary : objects) {
            String fileName = objectBinary.getPath();
            InputStream inputStream = objectBinary.getStream();

            zipOut.putNextEntry(new ZipEntry(fileName));

            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                zipOut.write(buffer, 0, len);
            }

            zipOut.closeEntry();
            inputStream.close();
        }

        zipOut.finish();
        zipOut.close();

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        InputStreamResource inputStreamResource = new InputStreamResource(byteArrayInputStream);
        return new CompressedObject(name + ".zip", inputStreamResource);
    }
}
