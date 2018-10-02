package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import helpers.HttpHelper;
import helpers.InventoryHelper;
import helpers.JsonHelper;
import helpers.RequestHelper;
import models.inventory.Inventory;
import models.inventory.InventoryItem;
import models.inventory.InventoryTransaction;
import play.mvc.Controller;
import play.mvc.Result;
import tyrex.services.UUID;

import javax.swing.text.html.Option;
import java.util.Optional;

public class InventoryController extends Controller {

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


    public Result createInventoryItem(String envId) {
        return RequestHelper.createEnvironmentModelRequestHandle(request(),
                                                                 InventoryItem.class,
                                                                 InventoryItem[].class,
                                                                 envId);
    }

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
}
