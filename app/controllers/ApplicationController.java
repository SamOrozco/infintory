package controllers;

import play.mvc.Controller;
import play.mvc.Result;

public class ApplicationController extends Controller {

    public Result options() {
        return ok("")
            .withHeader("Access-Control-Allow-Headers", "*")
            .withHeader("Access-Control-Allow-Credentials", "true")
            .withHeader("Connection", "keep-alive")
            .withHeader("access-control-max-age", "86400")
            .withHeader("Access-Control-Allow-Origin", "http://localhost:8080");
    }


    public Result optionss(String path) {
        return ok("")
            .withHeader("Access-Control-Allow-Headers", "*")
            .withHeader("Access-Control-Allow-Credentials", "true")
            .withHeader("Connection", "keep-alive")
            .withHeader("access-control-max-age", "86400")
            .withHeader("Access-Control-Allow-Origin", "http://localhost:8080");
    }
}
