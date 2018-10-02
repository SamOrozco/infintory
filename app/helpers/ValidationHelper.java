package helpers;

import java.util.Collection;

public class ValidationHelper {


    public static boolean NullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }

    public static <S> boolean NullOrEmpty(Collection<S> collection) {
        return collection == null || collection.isEmpty();
    }
}
