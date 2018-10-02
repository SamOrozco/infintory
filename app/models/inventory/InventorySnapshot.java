package models.inventory;

import models.shared.EnvironmentModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "inventory_snapshot")
public class InventorySnapshot extends EnvironmentModel {
    @Id
    @Column
    private int inventorySnapshotId;
    @Column
    private String snapshotKey;

    public int getInventorySnapshotId() {
        return inventorySnapshotId;
    }

    public void setInventorySnapshotId(int inventorySnapshotId) {
        this.inventorySnapshotId = inventorySnapshotId;
    }

    public String getSnapshotKey() {
        return snapshotKey;
    }

    public void setSnapshotKey(String snapshotKey) {
        this.snapshotKey = snapshotKey;
    }
}
