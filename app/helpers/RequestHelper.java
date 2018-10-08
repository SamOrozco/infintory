package helpers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Product;
import models.shared.EnvironmentModel;
import play.mvc.Http;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static play.mvc.Results.*;

public class RequestHelper {

    public static <S extends EnvironmentModel> Result createEnvironmentModelRequestHandle(
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


    public static <S> Result findByEnvironmentId(Class<S> clazz,
                                                 String envId) {
        try {
            List<S> ss = DatabaseHelper.findByEnvironmentId(clazz,
                                                            envId);
            if (ss == null) {
                ss = new ArrayList<>();
            }
            return ok(JsonHelper.serializeJson(ss));
        } catch (Exception e) {
            return internalServerError(e.getMessage());
        }
    }
}
