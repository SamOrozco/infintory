package helpers;

import models.Environment;

import java.util.UUID;

public class EnvironmentHelper {


    public static String newEnvironment() {
        String environmentKey = UUID.randomUUID().toString();
        try {
            new Environment(environmentKey).insert();
            return environmentKey;
        } catch (Exception e) {
            throw ErrorHelper.databaseInsertError("environment");
        }
    }
}
