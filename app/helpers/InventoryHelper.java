package helpers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import models.inventory.InventoryTransaction;

import java.util.*;

import static play.mvc.Results.badRequest;

public class InventoryHelper {
    private static final int MAX_TRANSACTION = 500;

    public static String handleTransactionItems(String inventoryKey,
                                                String environmentKey,
                                                InventoryTransaction... transactions) throws
                                                                                      Exception {
        if (transactions == null) {
            throw new RuntimeException("No transactions");
        }
        if (transactions.length > MAX_TRANSACTION) {
            throw new RuntimeException("Max allowed transactions is 500");
        }

        final String transactionId = UUID.randomUUID().toString();

        // this will hold distinct product Ids so we can validate the parts sent
        final Set<Integer> distinctProductIds = new HashSet<>();
        // this will hold the netChange per part so we only have to go through the trans once
        final Map<Integer, Double> netChange = new HashMap<>();
        // we need to first validate parts for every transaction
        // then we want to insert into the database
        for (InventoryTransaction tempTransaction : transactions) {
            int productId = tempTransaction.getProductId();
            double count = tempTransaction.getDifference();
            tempTransaction.setInventoryKey(inventoryKey);
            tempTransaction.setEnvironmentId(environmentKey);
            distinctProductIds.add(productId);
            netChange.computeIfPresent(productId, (key, value) -> value + count);
            netChange.putIfAbsent(productId, count);
        }

        Optional<Collection<Integer>> validateResult =
            ProductHelper.validateProductIds(distinctProductIds,
                                             environmentKey);
        if (validateResult.isPresent()) {
            throw ErrorHelper.invalidIdError(validateResult.get());
        }

        Transaction ebeanTransaction = Ebean.beginTransaction();
        ebeanTransaction.setBatchSize(30);

        try {
            for (Map.Entry<Integer, Double> entry : netChange.entrySet()) {
                InventoryTransaction transaction = new InventoryTransaction();
                transaction.setProductId(entry.getKey());
                transaction.setDifference(entry.getValue());
                transaction.setInventoryKey(inventoryKey);
                transaction.setTransactionKey(transactionId);
                transaction.setEnvironmentId(environmentKey);
                transaction.insert();
            }
            ebeanTransaction.commit();
            return transactionId;
        } catch (Exception e) {
            ebeanTransaction.rollback();
            throw new RuntimeException(
                String.format("Error inserting into databaes. %s", e.getMessage()));
        }

    }
}
