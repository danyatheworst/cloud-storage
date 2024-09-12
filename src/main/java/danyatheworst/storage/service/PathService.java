package danyatheworst.storage.service;

import org.springframework.stereotype.Component;

@Component
public class PathService {
    public String composeDir(String path, Long userId) {
        if (path.equals("/")) {
            return "user-" + userId + "-files/";
        }

        if (path.endsWith("/")) {
            return "user-" + userId + "-files/".concat(path);
        }

        return "user-" + userId + "-files/"
                .concat(path)
                .concat("/");
    }

    public String composeFile(String path, Long userId) {
        return "user-" + userId + "-files/".concat(path);
    }
}
