package controllers;

import helpers.DatabaseHelper;
import helpers.JsonHelper;
import helpers.ProductHelper;
import helpers.RequestHelper;
import models.Product;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.List;

public class ProductController extends Controller {

    public Result createProduct(String envId) {
        return RequestHelper.createEnvironmentModelRequestHandle(request(),
                                                                 Product.class,
                                                                 Product[].class,
                                                                 envId);
    }


    public Result getProducts(String envID) {
        return RequestHelper.findByEnvironmentId(Product.class,
                                                 envID);
    }
}
