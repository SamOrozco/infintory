package helpers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import com.fasterxml.jackson.databind.JsonNode;
import models.shared.EnvironmentModel;

import java.util.List;
import java.util.function.Consumer;

public class DatabaseHelper {


    public static <S extends EnvironmentModel> JsonNode insertEnvironmentModel(
        Class<S> single,
        Class<S[]> multi,
        JsonNode node,
        String envId,
        Consumer<S>... mutators) throws Exception {

        S product = null;
        S[] products = null;
        try {
            if (!node.isArray()) {
                product = JsonHelper.deserializeObject(single, node);
            } else {
                products = JsonHelper.deserializeArray(multi, node);
            }

            // mutate our objects before insertion
            applyMutators(product, products, mutators);
            return insertModels(product, products, envId);
        } catch (Exception e) {
            throw e;
        }
    }


    private static <S extends EnvironmentModel> JsonNode insertModels(S single,
                                                                      S[] multi,
                                                                      String envId) throws
                                                                                    Exception {
        if (single != null) {
            S prod = DatabaseHelper.insertSingle(single, envId);
            return JsonHelper.serializeJson(prod);
        }
        if (multi != null) {
            S[] prods = DatabaseHelper.insertMulti(multi, envId);
            return JsonHelper.serializeJson(prods);
        }
        throw new RuntimeException("Error inserting products");
    }

    public static <T extends EnvironmentModel> T insertSingle(T model, String envId) {
        model.setEnvironmentId(envId);
        model.insert();
        model.refresh();
        return model;
    }


    public static <T extends EnvironmentModel> T[] insertMulti(T[] models, String envId) {
        Transaction transaction = Ebean.beginTransaction();
        transaction.setBatchSize(30);
        try {
            for (EnvironmentModel model : models) {
                model.setEnvironmentId(envId);
                model.insert();
                model.refresh();
            }
            Ebean.commitTransaction();
            return models;
        } catch (Exception e) {
            transaction.rollback();
            throw e;
        }
    }


    public static <S> void applyMutators(S single, S[] multi, Consumer<S>... mutators) {
        if (single != null) {
            for (Consumer<S> mutator : mutators) {
                mutator.accept(single);
            }
        }
        if (multi != null) {

            // I don't like those numbers
            for (Consumer<S> mutator : mutators) {
                for (S m : multi) {
                    mutator.accept(m);
                }
            }
        }

    }


    public static <S> List<S> findByEnvironmentId(Class<S> clazz, String envId) {
        return Ebean.find(clazz)
                    .where()
                    .eq("env_id", envId)
                    .findList();
    }
}
