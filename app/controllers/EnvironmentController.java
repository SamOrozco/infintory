package controllers;

import models.Environment;
import play.mvc.Result;

import static helpers.HttpHelper.corsHeader;
import static play.mvc.Results.*;

public class EnvironmentController {


    public Result createEnvironment() {
        try {
            String environmentKey = Environment.newEnvironment();
            return corsHeader(ok(environmentKey));
        } catch (Exception e) {
            return internalServerError(e.getMessage());
        }
    }


}
