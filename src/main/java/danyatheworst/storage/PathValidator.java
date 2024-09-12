package danyatheworst.storage;

import danyatheworst.exceptions.InvalidParameterException;
import org.springframework.stereotype.Component;


@Component
public class PathValidator {

    public void validate(String path) {
        if (path == null || path.isEmpty()) {
            throw new InvalidParameterException("Path can't be null or empty");
        }

        if (this.endsWithTwoOrMoreSlashes(path)) {
            throw new InvalidParameterException("Path can't end with two or more slashes next to each other");
        }

        if (path.contains(":")) {
            throw new InvalidParameterException("Path can't contain a colum");
        }

        if (path.contains("\\")) {
            throw new InvalidParameterException("Path can't contain a backslash");
        }

        String[] segments = path.split("/");

        for (String segment : segments) {
            if (segment.startsWith(".")) {
                throw new InvalidParameterException("Path can't start with a dot");
            }

            if (segment.isEmpty()) {
                throw new InvalidParameterException("Path can't contain with two or more slashes next to each other");
            }
        }
    }

    private boolean endsWithTwoOrMoreSlashes(String input) {
        if (input == null || input.length() < 2) {
            return false;
        }

        int length = input.length();

        return input.charAt(length - 1) == '/' && input.charAt(length - 2) == '/';
    }
}
