package controllers;

import helpers.RequestHelper;
import models.Uom;
import play.mvc.Controller;
import play.mvc.Result;

public class UomController extends Controller {

    public Result createUom(String envId) {
        return RequestHelper.createEnvironmentModelRequestHandle(request(),
                                                                 Uom.class,
                                                                 Uom[].class,
                                                                 envId);
    }
}
