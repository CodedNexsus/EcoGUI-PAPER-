package rohit.EcoGUI.inventory;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Dedicated holder to identify the Sell GUI reliably (title-agnostic).
 * This helps ensure consistent behavior across Java/Bedrock clients
 * where titles or formatting may differ.
 */
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
