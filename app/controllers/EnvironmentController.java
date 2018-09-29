package controllers;

import helpers.EnvironmentHelper;
import play.mvc.Result;

import static play.mvc.Results.*;

public class EnvironmentController {


    public Result createEnvironment() {
        try {
            String environmentKey = EnvironmentHelper.newEnvironment();
            return ok(environmentKey);
        } catch (Exception e) {
            return internalServerError(e.getMessage());
        }
    }


}
