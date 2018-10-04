package models.inventory;

import models.shared.EnvironmentModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "inventory_item")
public class InventoryItem extends EnvironmentModel {
    @Id
    @Column
    private Integer inventoryItemId = null;
    @Column
    private String inventoryKey;
    @Column
    private int productId;
    @Column
    private Double count;


    /**
     * Combines inventory item and transaction item totals and returns a new inventory item with
     * new snapshot key. Assumes both products ids match.
     *
     * @param item
     * @param trans
     * @param newSnapshot
     * @return
     */
    public static InventoryItem combineNew(InventoryItem item,
                                           InventoryTransaction trans,
                                           String newSnapshot,
                                           String envId) {
        double total = trans.getDifference() + item.getCount();
        return new InventoryItem(total, item.getProductId(), newSnapshot, envId);
    }


    public InventoryItem() {
    }


    public InventoryItem(double count, int productId, String invKey, String envId) {
        this.count = count;
        this.productId = productId;
        this.inventoryKey = invKey;
        this.setEnvironmentId(envId);
    }

    public Integer getInventoryItemId() {
        return inventoryItemId;
    }

    public void setInventoryItemId(Integer inventoryItemId) {
        this.inventoryItemId = inventoryItemId;
    }

    public String getInventoryKey() {
        return inventoryKey;
    }

    public void setInventoryKey(String inventoryKey) {
        this.inventoryKey = inventoryKey;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public Double getCount() {
        return count;
    }

    public void setCount(Double count) {
        this.count = count;
    }
}
