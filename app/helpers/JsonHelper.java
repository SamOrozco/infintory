package helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonHelper {
    private static final ObjectMapper mapper = new ObjectMapper();


    public static <T> T deserializeJson(Class<T> clazz, JsonNode node) throws Exception {
        try {
            return mapper.treeToValue(node, clazz);
        } catch (JsonProcessingException e) {
            throw ErrorHelper.jsonParseError(e.getMessage());
        }
    }


    public static <T> JsonNode serializeJson(T t) throws Exception {
        return mapper.valueToTree(t);
    }
}
