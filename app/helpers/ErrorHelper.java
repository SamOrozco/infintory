package helpers;

public class ErrorHelper {

    public static RuntimeException databaseInsertError(String name) {
        return new RuntimeException(String.format("There was an issue inserting %s", name));
    }


    public static RuntimeException jsonParseError(String message) {
        return new RuntimeException(String.format("Error Parsing %s", message));
    }
}
