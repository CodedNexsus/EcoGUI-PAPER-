package rohit.EcoGUI.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import rohit.EcoGUI.Main;
import rohit.EcoGUI.inventory.SellInventoryHolder;
import rohit.EcoGUI.shop.SellingSystem;

import java.util.*;

/**
 * Sell GUI listener rewritten to:
 * - Work reliably across Java/Bedrock (Android) clients by identifying the GUI via InventoryHolder instead of title text
 * - Only process selling when the GUI is closed
 * - Return any unsellable items back to the player to avoid loss
 * - Prevent item dropping while the Sell GUI is open
 */
public class SellGUIListener implements Listener {

    private static final int GUI_SIZE = 54;           // 6 rows
    private static final int ITEM_AREA_END = 44;      // Slots 0..44 are item placement area
    private static final int CONTROL_ROW_START = 45;  // Slots 45..53 are control row
    private static final int CLOSE_BUTTON_SLOT = 49;  // Center of bottom row
    private static final String SELL_GUI_TITLE = "§6Sell Items"; // Title for display only (not used for detection)

    private final Main plugin;
    private final SellingSystem sellingSystem;

    public SellGUIListener(Main plugin) {
        this.plugin = plugin;
        this.sellingSystem = new SellingSystem(plugin, plugin.getEconomy());
    }

    // ===== Public API =====

    /**
     * Open the sell GUI for a player.
     * Players can place any items into slots 0..44. Bottom row is reserved controls.
     */
    public void openSellGUI(Player player) {
        Inventory sellInventory = Bukkit.createInventory(new SellInventoryHolder(null), GUI_SIZE, SELL_GUI_TITLE);

        // Fill the control row with panes, keeping a close & sell button in the center
        ItemStack pane = createGlassPane();
        for (int i = CONTROL_ROW_START; i < GUI_SIZE; i++) {
            if (i == CLOSE_BUTTON_SLOT) continue;
            sellInventory.setItem(i, pane);
        }
        sellInventory.setItem(CLOSE_BUTTON_SLOT, createCloseButton());

        player.openInventory(sellInventory);
    }

    // ===== Event Handlers =====

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryView view = event.getView();
        if (!isSellGui(view)) return;

        Player player = (Player) event.getWhoClicked();
        Inventory clicked = event.getClickedInventory();
        if (clicked == null) return;

        Inventory top = view.getTopInventory();

        // Clicking inside our Sell GUI (top inventory)
        if (clicked.equals(top)) {
            int slot = event.getSlot();

            // Allow free placement in item area 0..44
            if (slot >= 0 && slot <= ITEM_AREA_END) {
                return; // not cancelled
            }

            // Block control row except for the close button
            event.setCancelled(true);
            if (slot == CLOSE_BUTTON_SLOT) {
                player.closeInventory();
            }
            return;
        }

        // Clicking in player inventory (bottom): allow moves (including shift-click into top)
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!isSellGui(event.getView())) return;

        // Only allow dragging into the item area 0..44; block control row 45..53
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot < GUI_SIZE && rawSlot > ITEM_AREA_END) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!isSellGui(event.getView())) return;

        Player player = (Player) event.getPlayer();
        Inventory inv = event.getInventory();

        // Collect all items from the item area and compute totals
        Map<Material, Integer> soldCounts = new HashMap<>();
        List<ItemStack> refundItems = new ArrayList<>();
        List<ItemStack> allItemsForFailRefund = new ArrayList<>();
        double totalValue = 0D;

        for (int i = 0; i <= ITEM_AREA_END; i++) {
            ItemStack it = inv.getItem(i);
            if (it == null || it.getType() == Material.AIR) continue;

            // Keep a clone so we can refund on economy failure without relying on inventory state
            allItemsForFailRefund.add(it.clone());

            double unitSell = sellingSystem.getItemSellPrice(it.getType());
            if (unitSell > 0) {
                soldCounts.merge(it.getType(), it.getAmount(), Integer::sum);
                totalValue += unitSell * it.getAmount();
            } else {
                refundItems.add(it.clone());
            }
        }

        // Nothing in the GUI
        if (soldCounts.isEmpty() && refundItems.isEmpty()) {
            player.sendMessage("§7No items to sell!");
            return;
        }

        // Process economy deposit only for sellable items
        boolean depositOk = true;
        if (totalValue > 0) {
            depositOk = sellingSystem.depositMoney(player, totalValue);
        }

        // If economy failed, refund everything (sold + unsellable)
        if (!depositOk) {
            for (ItemStack stack : allItemsForFailRefund) {
                addOrDrop(player, stack);
            }
            player.sendMessage("§c❌ Transaction failed!");
            // Clear GUI slots to avoid dupes
            clearItemArea(inv);
            return;
        }

        // Economy succeeded: refund only the unsellable items
        for (ItemStack stack : refundItems) {
            addOrDrop(player, stack);
        }

        // Clear GUI item area so sold items are consumed
        clearItemArea(inv);

        // Messaging for success
        if (totalValue > 0) {
            player.sendMessage("§a✅ Items sold successfully!");
            player.sendMessage("§7Total Earned: §a" + sellingSystem.formatPrice(totalValue));
            player.sendMessage("§7New Balance: §a" + sellingSystem.formatPrice(sellingSystem.getPlayerBalance(player)));

            // Log a short summary
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<Material, Integer> e : soldCounts.entrySet()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(e.getValue()).append("x ").append(e.getKey().name());
            }
            plugin.getLogger().info(player.getName() + " sold via Sell GUI: " + sb + " for " + totalValue);
        } else {
            // Only unsellable items were present
            player.sendMessage("§7No sellable items found. Returned items to your inventory.");
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        InventoryView view = player.getOpenInventory();
        if (view != null && view.getTopInventory() != null && view.getTopInventory().getHolder() instanceof SellInventoryHolder) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + " You cannot drop items while the Sell GUI is open.");
        }
    }

    // ===== Helpers =====

    private boolean isSellGui(InventoryView view) {
        Inventory top = view.getTopInventory();
        return top != null && top.getHolder() instanceof SellInventoryHolder;
    }

    private void clearItemArea(Inventory inv) {
        for (int i = 0; i <= ITEM_AREA_END; i++) {
            inv.setItem(i, null);
        }
    }

    private void addOrDrop(Player player, ItemStack stack) {
        if (stack == null || stack.getType() == Material.AIR || stack.getAmount() <= 0) return;
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(stack);
        if (!leftover.isEmpty()) {
            for (ItemStack l : leftover.values()) {
                if (l != null && l.getType() != Material.AIR && l.getAmount() > 0) {
                    player.getWorld().dropItemNaturally(player.getLocation(), l);
                }
            }
        }
    }

    private ItemStack createGlassPane() {
        ItemStack pane = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            pane.setItemMeta(meta);
        }
        return pane;
    }

    private ItemStack createCloseButton() {
        ItemStack closeButton = new ItemStack(Material.BARRIER);
        ItemMeta meta = closeButton.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c✕ Close & Sell");
            closeButton.setItemMeta(meta);
        }
        return closeButton;
    }
}
