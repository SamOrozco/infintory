package helpers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import models.inventory.Inventory;
import models.inventory.InventoryItem;
import models.inventory.InventorySnapshot;
import models.inventory.InventoryTransaction;

import java.util.*;

import static helpers.ValidationHelper.NullOrEmpty;

public class InventoryHelper {
    private static final int MAX_TRANSACTION = 10 * 1000;


    public static void rollbackTransaction(String invKey,
                                           String transKey) {
        int maxId = getMaxTransactionIdForKey(transKey);
        Ebean.find(InventoryTransaction.class)
             .where()
             .gt("transaction_id", maxId)
             .delete();
    }


    /**
     * selecting the max transaction id from where the transaction key is equal to the given key
     *
     * @param transKey
     * @return
     */
    public static int getMaxTransactionIdForKey(String transKey) {
        List<InventoryTransaction> transactions = Ebean.find(InventoryTransaction.class)
                                                       .where()
                                                       .eq("transaction_key", transKey)
                                                       .orderBy()
                                                       .desc("transaction_id")
                                                       .setFirstRow(0)
                                                       .setMaxRows(1)
                                                       .findPagedList()
                                                       .getList();
        if (NullOrEmpty(transactions)) {
            throw new RuntimeException("Invalid transaction key");
        }
        return transactions.get(0).getTransactionId();
    }

    /**
     * delets all transacitons with given trans action id
     *
     * @param transactionId
     */
    public static void clearTransaction(String transactionId) {
        Ebean.find(InventoryTransaction.class)
             .where()
             .eq("transaction_key", transactionId)
             .delete();
    }

    /**
     * Removes all transactions that have the specified envId and inventory key
     *
     * @param envId
     * @param invId
     */
    public static void cleanTransactions(String envId,
                                         String invId) {
        Ebean.find(InventoryTransaction.class)
             .where()
             .eq("env_id", envId)
             .eq("inventory_key", invId)
             .delete();
    }

    /**
     * Creates a new inventory snapshot with the specified envId
     *
     * @param envId
     * @return
     */
    public static InventorySnapshot initInventorySnapshot(String envId) {
        String snapshotKey = UUID.randomUUID().toString();
        InventorySnapshot shot = new InventorySnapshot(snapshotKey, envId);
        shot.insert();
        shot.refresh(); // get the generated stuff
        return shot;
    }


    /**
     * Gets all transactions for the given inventory Id
     *
     * @param invId
     * @return
     */
    public static List<InventoryTransaction> getTransactionsForInv(String invId) {
        return Ebean.find(InventoryTransaction.class)
                    .where()
                    .eq("inventory_key", invId)
                    .orderBy()
                    .asc("transaction_id")
                    .findList();
    }


    /**
     * Gets all transactions for the given inventory Id
     *
     * @param invId
     * @return
     */
    public static List<InventoryTransaction> getActiveTransactionsForInv(String invId) {
        return Ebean.find(InventoryTransaction.class)
                    .where()
                    .eq("inventory_key", invId)
                    .orderBy()
                    .asc("transaction_id")
                    .findList();
    }


    /**
     * finds a transaction by the given transaction key
     *
     * @param transKey
     * @return
     */
    public static List<InventoryTransaction> getTransactionByKey(String transKey) {
        return Ebean.find(InventoryTransaction.class)
                    .where()
                    .eq("transaction_key", transKey)
                    .findList();
    }


    /**
     * this method gets an inventory snapshot by key
     *
     * @param snapshotKey
     * @return
     */
    public static Optional<InventorySnapshot> getSnapshotByKey(String snapshotKey) {
        return Optional.ofNullable(Ebean.find(InventorySnapshot.class)
                                        .where()
                                        .eq("snapshot_key", snapshotKey)
                                        .findUnique());
    }


    /**
     * Gets a map of productId -> InventoryItem for the given snapshot key
     *
     * @param snapshotKey
     * @return
     */
    public static Map<Integer, InventoryItem> getSnapshotItemMap(String snapshotKey) {
        List<InventoryItem> items = Ebean.find(InventoryItem.class)
                                         .where()
                                         .eq("inventory_key", snapshotKey)
                                         .findList();
        if (NullOrEmpty(items)) {
            return new HashMap<>();
        }

        return CollectionHelper.mapToProperty(items,
                                              InventoryItem::getProductId);
    }


    /**
     * Get's an inventory object by the given key
     *
     * @param key
     * @return
     */
    public static Optional<Inventory> findInventoryByKey(String key) {
        return Optional.ofNullable(Ebean.find(Inventory.class)
                                        .where()
                                        .eq("inventory_key", key)
                                        .findUnique());
    }


    /**
     * Creates transactions with the specified invId and envId
     * This validates all parts in the transactions
     * Updates the inventory with the key passed to point to the newly creates transaction
     * returns transaction key
     *
     * @param inventoryKey
     * @param environmentKey
     * @param transactions
     * @return
     * @throws Exception
     */
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
            Optional<Inventory> inventory = InventoryHelper.findInventoryByKey(inventoryKey);
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
                String.format("Error inserting into database. %s", e.getMessage()));
        }
    }


    /**
     * Compile the previous snapshot total with the current list of transaction totals
     * We assume all product ids are validate by this point so we will not validate.
     *
     * @param invItemMap
     * @param transactions
     * @return
     */
    public static Map<Integer, InventoryItem> compileInventory(
        Map<Integer, InventoryItem> invItemMap,
        List<InventoryTransaction> transactions,
        String newSnapshotKey,
        String envId) {
        if (NullOrEmpty(transactions)) return invItemMap;
        if (invItemMap == null) invItemMap = new HashMap<>();
        Map<Integer, InventoryItem> resultMap = new HashMap<>();
        for (InventoryTransaction trans : transactions) {
            double diff = trans.getDifference();
            int prodId = trans.getProductId();
            InventoryItem resItem = resultMap.get(prodId);
            InventoryItem existItem = invItemMap.get(prodId);
            if (resItem != null) { // we have already seen this prod and put in resMap
                double totalDiff = diff + resItem.getCount();
                resultMap.put(prodId, new InventoryItem(totalDiff,
                                                        prodId,
                                                        newSnapshotKey,
                                                        envId));
            } else if (existItem != null) { // there is an item in the map and we haven't reached it
                double totalDiff = diff + existItem.getCount();
                resultMap.put(prodId, new InventoryItem(totalDiff,
                                                        prodId,
                                                        newSnapshotKey,
                                                        envId));
            } else { // net new item
                resultMap.put(prodId, new InventoryItem(diff,
                                                        prodId,
                                                        newSnapshotKey,
                                                        envId));
            }
        }
        return resultMap;
    }


    /**
     * returns a list of current totals from snapshot and any current transactions
     *
     * @param invId
     * @return
     */
    public static Collection<InventoryItem> totalInventory(String invId, String envId) throws
                                                                                       Exception {
        Optional<Inventory> optInv = findInventoryByKey(invId);
        if (!optInv.isPresent()) {
            throw new RuntimeException(String.format("Inventory with key %s does not exist.",
                                                     invId));
        }
        Inventory inv = optInv.get();
        String currentSnapKey = inv.getCurrentSnapshot();
        String currentTransactionKey = inv.getCurrentTransaction();

        Map<Integer, InventoryItem> totalMap = null;
        List<InventoryTransaction> transactions = null;

        // fresh inventory with no transactions now
        if (NullOrEmpty(currentSnapKey) && NullOrEmpty(currentTransactionKey)) {
            return Collections.emptyList();
        }
        if (!NullOrEmpty(currentSnapKey)) {
            totalMap = getSnapshotItemMap(currentSnapKey);
        }
        if (!NullOrEmpty(currentTransactionKey)) {
            transactions = getActiveTransactionsForInv(invId);
        }
        return compileInventory(totalMap,
                                transactions,
                                invId,
                                envId).values();
    }
}
