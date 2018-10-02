package controllers;

import helpers.RequestHelper;
import models.Product;
import play.mvc.Controller;
import play.mvc.Result;

public class ProductController extends Controller {

    public Result createProduct(String envId) {
        return RequestHelper.createEnvironmentModelRequestHandle(request(),
                                                                 Product.class,
                                                                 Product[].class,
                                                                 envId);
    }


    public Result getProducts(String envID) {
        return ok();
    }
}
