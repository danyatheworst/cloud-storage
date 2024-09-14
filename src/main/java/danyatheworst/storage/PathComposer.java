package danyatheworst.storage;

import org.springframework.stereotype.Component;

@Component
public class PathComposer {
    //TODO: it'd be cool to init that bean with actual userId
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

    public String removeRoot(String path, Long userId) {
        String root = "user-" + userId + "-files/";
        return path.substring(root.length());
    }
}
