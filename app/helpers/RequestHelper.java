package helpers;

import com.fasterxml.jackson.databind.JsonNode;
import models.shared.EnvironmentModel;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static play.mvc.Results.*;

public class RequestHelper {

    public static <S extends EnvironmentModel, R, R1> Result createEnvironmentModelRequestHandle(
        Http.Request request,
        Class<S> single,
        Class<S[]> multi,
        String envId,
        Consumer<S>... mutators) {
        Optional<JsonNode> bodyNode = HttpHelper.getJsonBody(request);
        if (!bodyNode.isPresent()) {
            return badRequest("invalid body");
        }
        JsonNode node = bodyNode.get();

        try {
            JsonNode resultNode = DatabaseHelper.insertEnvironmentModel(single,
                                                                        multi,
                                                                        node,
                                                                        envId,
                                                                        mutators);
            return ok(resultNode);
        } catch (Exception e) {
            return internalServerError(e.getMessage());
        }
    }
}
