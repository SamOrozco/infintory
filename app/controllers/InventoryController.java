package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import com.fasterxml.jackson.databind.JsonNode;
import helpers.*;
import models.inventory.*;
import play.mvc.Controller;
import play.mvc.Result;
import tyrex.services.UUID;

import java.util.*;

import static helpers.InventoryHelper.getTransactionsForInv;
import static helpers.ValidationHelper.NullOrEmpty;

public class InventoryController extends Controller {


    public Result getInventoryTransaction(String inventoryTransaction) {
        try {
            List<InventoryTransaction> transactions =
                InventoryHelper.getTransactionByKey(inventoryTransaction);
            if (transactions == null) {
                return notFound(
                    String.format("Transaction with key %s does not exist", inventoryTransaction));
            }
            return ok(JsonHelper.serializeJson(transactions));
        } catch (Exception e) {
            return internalServerError(e.getMessage());
        }
    }


    /**
     * Rolls inventory with given key back to Snapshot with given key this is a power full method.
     * Being able to point an inventory to any snapshot. With great power comes great responsibility
     *
     * @param invKey
     * @param snapshotkey
     * @return
     */
    public Result rollbackToSnapshot(String invKey,
                                     String snapshotkey) {
        Optional<Inventory> optInv = InventoryHelper.findInventoryByKey(invKey);
        if (!optInv.isPresent()) {
            return notFound(String.format("Inventory with key %s not found", invKey));
        }

        Optional<InventorySnapshot> optSnap = InventoryHelper.getSnapshotByKey(snapshotkey);
        if (!optSnap.isPresent()) {
            return notFound(String.format("Inventory snapshot with key %s not found", snapshotkey));
        }
        try {
            Inventory inventory = optInv.get();
            // rollback to snapshot
            inventory.setCurrentSnapshot(snapshotkey);
            // reset transactions
            inventory.setCurrentTransaction(null);
            inventory.update();
            return ok();
        } catch (Exception e) {
            return internalServerError("Error rolling back to snapshot, %s", e.getMessage());
        }
    }


    /**
     * rolls back an inventory to the given transaction Id
     *
     * @param invId
     * @param transactionId
     * @return
     */
    public Result rollbackToTransaction(String invId,
                                        String transactionId) {
        try {
            InventoryHelper.rollbackTransaction(invId, transactionId);
            return ok();
        } catch (Exception e) {
            return internalServerError(e.getMessage());
        }
    }


    /**
     * Creates a new inventory and returns inventory key
     *
     * @param envId
     * @return
     */
    public Result createInventory(String envId) {
        return RequestHelper.createEnvironmentModelRequestHandle(request(),
                                                                 Inventory.class,
                                                                 Inventory[].class,
                                                                 envId,
                                                                 inv -> {
                                                                     String invKey = UUID.create();
                                                                     inv.setInventoryKey(invKey);
                                                                 });
    }


    /**
     * Gets all inventory objects for an environment
     *
     * @param envId
     * @return
     */
    public Result getInventory(String envId) {
        return RequestHelper.findByEnvironmentId(Inventory.class,
                                                 envId);
    }

    /**
     * This methods returns a list of product total for an inventory
     *
     * @param invKey
     * @return
     */
    public Result getInventoryTotals(String envId, String invKey) {
        try {
            return ok(JsonHelper.serializeJson(InventoryHelper.totalInventory(invKey, envId)));
        } catch (Exception e) {
            return internalServerError(e.getMessage());
        }
    }


    /**
     * deletes all inventoryTransaction with given key
     *
     * @param transId
     * @return
     */
    public Result revertTransaction(String transId) {
        try {
            InventoryHelper.clearTransaction(transId);
            return ok();
        } catch (Exception e) {
            return internalServerError(e.getMessage());
        }
    }


    /**
     * returns all transactions for the given inventory key
     *
     * @param inventoryKey
     * @return
     */
    public Result getInventoryTransactions(String inventoryKey) {
        try {
            List<InventoryTransaction> transactions = getTransactionsForInv(inventoryKey);
            if (NullOrEmpty(transactions)) {
                return ok();
            }

            return ok(JsonHelper.serializeJson(transactions));
        } catch (Exception e) {
            return internalServerError(e.getMessage());
        }
    }


    /**
     * This method creates an inventory transaction from a body of transaction items
     * all marked with the same transaction key
     *
     * @param envID
     * @param invID
     * @return
     */
    public Result inventoryTransaction(String envID,
                                       String invID) {
        Optional<JsonNode> node = HttpHelper.getJsonBody(request());
        if (!node.isPresent()) {
            return internalServerError("Invalid Body");
        }

        JsonNode body = node.get();

        try {

            if (body.isArray()) {
                InventoryTransaction[] items =
                    JsonHelper.deserializeArray(InventoryTransaction[].class,
                                                body);
                String transKey = InventoryHelper.handleTransactionItems(invID,
                                                                         envID,
                                                                         items);
                return ok(transKey);
            } else {
                InventoryTransaction item = JsonHelper.deserializeObject(InventoryTransaction.class,
                                                                         body);
                String transKey = InventoryHelper.handleTransactionItems(invID,
                                                                         envID,
                                                                         item);

                return ok(transKey);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest(e.getMessage());
        }
    }


    /**
     * The method commits the inventory. Compiles net transaction values and adds to the previous
     * snapshots inventory items. It then creates a new inventory snapshot from the new totals and sets
     * the new snapshot as the inventory base snapshot. It also sets the current inventory transaction to null.
     * <p>
     * transaction difference
     *
     * @param envId
     * @param invId
     * @return
     */
    public Result commitInventory(String envId, String invId) {
        long startFunc = System.currentTimeMillis();

        // validating inventory exists
        Optional<Inventory> optInv = InventoryHelper.findInventoryByKey(invId);
        if (!optInv.isPresent()) {
            return badRequest(String.format("Inventory %s does not exist.", invId));
        }
        Inventory inventory = optInv.get();


        // loading our previous snapshot total and transactions since the last
        // commit if they exist
        String curSnapshotKey = inventory.getCurrentSnapshot();
        String curTransactionKey = inventory.getCurrentTransaction();

        Map<Integer, InventoryItem> productItemMap = null;
        List<InventoryTransaction> transactions = null;
        if (!NullOrEmpty(curSnapshotKey)) {
            //productId -> InvItem
            productItemMap = InventoryHelper.getSnapshotItemMap(curSnapshotKey);
        }
        if (!NullOrEmpty(curTransactionKey)) {
            transactions = getTransactionsForInv(invId);
        }


        // starting transaction so we can back out of new snapshot creation if anything
        // goes wrong
        Transaction transaction = Ebean.beginTransaction();
        transaction.setBatchSize(50);

        InventorySnapshot newSnap = InventoryHelper.initInventorySnapshot(envId);
        // all new inventory items have the new snapshot key
        Map<Integer, InventoryItem> totalMap = InventoryHelper.compileInventory(productItemMap,
                                                                                transactions,
                                                                                newSnap
                                                                                    .getSnapshotKey(),
                                                                                envId);

        try {
            Optional<Collection<Integer>> invalidProductIds =
                ProductHelper.validateProductIds(new ArrayList<>(totalMap.keySet()),
                                                 envId);
            if (invalidProductIds.isPresent()) { // any product ids present are invalid
                throw ErrorHelper.invalidIdError(invalidProductIds.get());
            }

            for (InventoryItem item : totalMap.values()) {
                item.insert();
                item.refresh();
            }
            inventory.setCurrentSnapshot(newSnap.getSnapshotKey());
            inventory.update();

            InventoryHelper.cleanTransactions(envId, invId);
            transaction.commit();
            return ok(
                JsonHelper.serializeJson(new InventorySnapshotResponse(newSnap.getSnapshotKey(),
                                                                       totalMap.values())));
        } catch (Exception e) {
            transaction.rollback();
            return internalServerError(e.getMessage());
        }


    }
}
