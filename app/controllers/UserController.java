package controllers;

import play.mvc.Controller;
import play.mvc.Result;

import static helpers.HttpHelper.corsHeader;

public class UserController extends Controller {


    public Result createUser() {
        return corsHeader(ok());
    }
}
