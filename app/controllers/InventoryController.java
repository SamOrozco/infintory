package controllers;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import com.fasterxml.jackson.databind.JsonNode;
import helpers.*;
import models.inventory.Inventory;
import models.inventory.InventoryItem;
import models.inventory.InventorySnapshot;
import models.inventory.InventoryTransaction;
import play.mvc.Controller;
import play.mvc.Result;
import tyrex.services.UUID;

import java.util.*;

import static helpers.InventoryHelper.getTransactionsForInv;
import static helpers.ValidationHelper.NullOrEmpty;

public class InventoryController extends Controller {


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
     * Creates an inventory item based on corresponding json body single or multi
     *
     * @param envId
     * @return
     */
    public Result createInventoryItem(String envId) {
        return RequestHelper.createEnvironmentModelRequestHandle(request(),
                                                                 InventoryItem.class,
                                                                 InventoryItem[].class,
                                                                 envId);
    }


    /**
     * returns all transactions for the given inventory key
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
            productItemMap = InventoryHelper.getSnapshotItemMap(envId,
                                                                curSnapshotKey);
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
        Map<Integer, InventoryItem> totalMap = compileInventory(productItemMap,
                                                                transactions,
                                                                newSnap.getSnapshotKey(),
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
            }
            inventory.setCurrentSnapshot(newSnap.getSnapshotKey());
            inventory.update();

            InventoryHelper.cleanTransactions(envId, invId);
            transaction.commit();
            return ok();
        } catch (Exception e) {
            transaction.rollback();
            return internalServerError(e.getMessage());
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
    private Map<Integer, InventoryItem> compileInventory(Map<Integer, InventoryItem> invItemMap,
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
}
