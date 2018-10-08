package helpers;

import com.fasterxml.jackson.databind.JsonNode;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Optional;

public class HttpHelper {

    public static Optional<JsonNode> getJsonBody(Http.Request request) {
        if (request == null) return Optional.empty();
        JsonNode node = request.body().asJson();
        return Optional.of(node);
    }

    public static Result corsHeader(Result result) {
        return result.withHeader("Access-Control-Allow-Origin", "*");
    }
}
