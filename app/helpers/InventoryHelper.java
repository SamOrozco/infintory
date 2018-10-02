package helpers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import models.inventory.Inventory;
import models.inventory.InventoryItem;
import models.inventory.InventoryTransaction;

import java.util.*;

import static helpers.ValidationHelper.NullOrEmpty;

public class InventoryHelper {
    private static final int MAX_TRANSACTION = 500;


    public static List<InventoryTransaction> getTransactionsForInv(String invId,
                                                                   String currentTransaction) {
        InventoryTransaction transaction = getTransactionByKey(currentTransaction);
        return Ebean.find(InventoryTransaction.class)
                    .where()
                    .eq("inventory_key", invId)
                    .eq("transaction_key", currentTransaction)
                    .lt("transaction_id", transaction.getTransactionId())
                    .findList();
    }


    public static InventoryTransaction getTransactionByKey(String transKey) {
        return Ebean.find(InventoryTransaction.class)
                    .where()
                    .eq("transaction_key", transKey)
                    .findUnique();
    }

    public static Map<Integer, InventoryItem> getSnapshotItemMap(String envId, String snapshotKey) {
        List<InventoryItem> items = Ebean.find(InventoryItem.class)
                                         .where()
                                         .eq("env_id", envId)
                                         .eq("snapshot_key", snapshotKey)
                                         .findList();
        if (NullOrEmpty(items)) {
            return new HashMap<>();
        }

        return CollectionHelper.mapToProperty(items,
                                              InventoryItem::getProductId);
    }


    public static Optional<Inventory> findByKey(String key) {
        return Optional.ofNullable(Ebean.find(Inventory.class)
                                        .where()
                                        .eq("inventory_key", key)
                                        .findUnique());
    }

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
            Optional<Inventory> inventory = InventoryHelper.findByKey(inventoryKey);
            if (!inventory.isPresent()) {
                throw new RuntimeException("Inventory doesn't exist");
            }
            for (Map.Entry<Integer, Double> entry : netChange.entrySet()) {
                InventoryTransaction transaction = new InventoryTransaction();
                transaction.setProductId(entry.getKey());
                transaction.setDifference(entry.getValue());
                transaction.setInventoryKey(inventoryKey);
                transaction.setTransactionKey(transactionId);
                transaction.setEnvironmentId(environmentKey);
                transaction.insert();
            }
            Inventory finalInv = inventory.get();
            finalInv.setCurrentTransaction(transactionId);
            finalInv.update();
            ebeanTransaction.commit();
            return transactionId;
        } catch (Exception e) {
            ebeanTransaction.rollback();
            throw new RuntimeException(
                String.format("Error inserting into databaes. %s", e.getMessage()));
        }

    }
}
