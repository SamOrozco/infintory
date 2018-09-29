package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import helpers.HttpHelper;
import helpers.JsonHelper;
import models.Product;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.Optional;

public class ProductController extends Controller {

    public Result createProduct(String envID) {
        Optional<JsonNode> bodyNode = HttpHelper.getJsonBody(request());
        if (!bodyNode.isPresent()) {
            return badRequest("invalid body");
        }
        JsonNode node = bodyNode.get();

        Product product = null;
        Product[] products = null;
        try {
            if (node.isArray()) {
                products = JsonHelper.deserializeJson(Product[].class, node);
            } else {
                product = JsonHelper.deserializeJson(Product.class, node);
            }

            JsonNode resultNode = insertProducts(product, products);
            return ok(resultNode);
        } catch (Exception e) {
            return internalServerError(e.getMessage());
        }
    }


    private JsonNode insertProducts(Product product, Product[] products) {

    }


    public Result getProducts(String envID) {
        return ok();
    }
}
