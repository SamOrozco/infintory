package helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

public class JsonHelper {
    private static final ObjectMapper mapper;
    static {
        mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
    }


    public static <T> T deserializeObject(Class<T> clazz, JsonNode node) throws Exception {
        try {
            return mapper.treeToValue(node, clazz);
        } catch (JsonProcessingException e) {
            throw ErrorHelper.jsonParseError(e.getMessage());
        }
    }

    public static <T> T[] deserializeArray(Class<T[]> clazz, JsonNode node) throws Exception {
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
