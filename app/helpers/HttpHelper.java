package helpers;

import com.fasterxml.jackson.databind.JsonNode;
import play.mvc.Http;

import java.util.Optional;

public class HttpHelper {

    public static Optional<JsonNode> getJsonBody(Http.Request request) {
        if (request == null) return Optional.empty();
        JsonNode node = request.body().asJson();
        return Optional.of(node);
    }
}
