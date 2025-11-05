package rohit.EcoGUI.inventory;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class SellInventoryHolder implements InventoryHolder {

    private final Inventory inventory;

    public SellInventoryHolder(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
