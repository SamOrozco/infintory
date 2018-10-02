package models.inventory;

import models.shared.EnvironmentModel;
import org.joda.time.DateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "inventory")
public class Inventory extends EnvironmentModel {

    @Id
    @Column
    private int inventoryId;
    @Column
    private String inventoryKey;
    @Column
    private String inventoryAlias;
    @Column
    private String currentSnapshot;
    @Column
    private String currentTransaction;

    public int getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(int inventoryId) {
        this.inventoryId = inventoryId;
    }

    public String getInventoryAlias() {
        return inventoryAlias;
    }

    public void setInventoryAlias(String inventoryAlias) {
        this.inventoryAlias = inventoryAlias;
    }

    public String getCurrentSnapshot() {
        return currentSnapshot;
    }

    public void setCurrentSnapshot(String currentSnapshot) {
        this.currentSnapshot = currentSnapshot;
    }

    public String getCurrentTransaction() {
        return currentTransaction;
    }

    public void setCurrentTransaction(String currentTransaction) {
        this.currentTransaction = currentTransaction;
    }

    public String getInventoryKey() {
        return inventoryKey;
    }

    public void setInventoryKey(String inventoryKey) {
        this.inventoryKey = inventoryKey;
    }
}
