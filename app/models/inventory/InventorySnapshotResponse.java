package models.inventory;

import java.util.Collection;

public class InventorySnapshotResponse {
    private String snapshotKey;
    private Collection<InventoryItem> items;

    public InventorySnapshotResponse(String snapshotKey,
                                     Collection<InventoryItem> items) {
        this.snapshotKey = snapshotKey;
        this.items = items;
    }


    public String getSnapshotKey() {
        return snapshotKey;
    }

    public void setSnapshotKey(String snapshotKey) {
        this.snapshotKey = snapshotKey;
    }

    public Collection<InventoryItem> getItems() {
        return items;
    }

    public void setItems(Collection<InventoryItem> items) {
        this.items = items;
    }
}
