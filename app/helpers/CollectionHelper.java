package helpers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static helpers.ValidationHelper.NullOrEmpty;

public class CollectionHelper {


    public static <K, V> Map<K, V> mapToProperty(Collection<V> collection, Function<V, K> keyFunc) {
        Map<K, V> resultMap = new HashMap<>();
        if (NullOrEmpty(collection)) {
            return resultMap;
        }
        for (V v : collection) {
            K key = keyFunc.apply(v);
            if (key != null) {
                resultMap.put(key, v);
            }
        }
        return resultMap;
    }
}
