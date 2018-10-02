package helpers;

import java.util.Collection;

public class ErrorHelper {

    public static RuntimeException databaseInsertError(String name) {
        return new RuntimeException(String.format("There was an issue inserting %s", name));
    }


    public static RuntimeException jsonParseError(String message) {
        return new RuntimeException(String.format("Error Parsing %s", message));
    }

    public static RuntimeException invalidIdError(Collection<Integer> ids) {
        StringBuilder builder = new StringBuilder("Invalid Product Ids:");
        for (Integer id : ids) {
            builder.append(String.format(" %s ", id));
        }
        return new RuntimeException(builder.toString());
    }
}
