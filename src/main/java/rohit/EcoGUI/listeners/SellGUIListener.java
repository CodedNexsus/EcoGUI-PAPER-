package rohit.EcoGUI.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import rohit.EcoGUI.Main;
import rohit.EcoGUI.inventory.ShopInventoryHolder;
import rohit.EcoGUI.shop.SellingSystem;

import java.util.HashMap;
import java.util.Map;

public class SellGUIListener implements Listener {

    private Main plugin;
    private SellingSystem sellingSystem;
    private static final String SELL_GUI_TITLE = "§6Sell Items";

    public SellGUIListener(Main plugin) {
        this.plugin = plugin;
        this.sellingSystem = new SellingSystem(plugin, plugin.getEconomy());
    }

    /**
     * Open the sell GUI for a player
     */
    public void openSellGUI(Player player) {
        // Create a 54-slot inventory (6 rows)
        Inventory sellInventory = Bukkit.createInventory(new ShopInventoryHolder(null), 54, SELL_GUI_TITLE);

        // Slots 0-44 (first 5 rows) are left completely empty for players to place items
        // No glass panels here - players can place items freely

        // Fill only the last row (slots 45-53) with glass panels, except for the close button
        ItemStack glassPane = createGlassPane();
        
        // Fill slots 45-48 (left side of last row)
        for (int i = 45; i < 49; i++) {
            sellInventory.setItem(i, glassPane);
        }
        
        // Fill slots 50-53 (right side of last row)
        for (int i = 50; i < 54; i++) {
            sellInventory.setItem(i, glassPane);
        }

        // Add close button at center of last row (slot 49)
        ItemStack closeButton = createCloseButton();
        sellInventory.setItem(49, closeButton);

        player.openInventory(sellInventory);
    }

    /**
     * Handle inventory click event for sell GUI
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryView view = event.getView();
        String title = view.getTitle();
        
        // Check if this is the sell GUI
        if (!title.equals(SELL_GUI_TITLE)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        Inventory clickedInventory = event.getClickedInventory();
        Inventory topInventory = view.getTopInventory();
        Inventory bottomInventory = view.getBottomInventory();

        // If clicking on the GUI inventory (top inventory)
        if (clickedInventory != null && clickedInventory.equals(topInventory)) {
            // Allow clicks on slots 0-44 (item placement area)
            if (slot >= 0 && slot < 45) {
                // Allow item placement and movement freely
                return;
            }

            // Cancel clicks on glass panels (slots 45-48 and 50-53)
            if ((slot >= 45 && slot < 49) || (slot >= 50 && slot < 54)) {
                event.setCancelled(true);
                return;
            }

            // Handle close button click (slot 49)
            if (slot == 49) {
                event.setCancelled(true);
                player.closeInventory();
                return;
            }
        }

        // If clicking on player inventory (bottom inventory)
        if (clickedInventory != null && clickedInventory.equals(bottomInventory)) {
            // Allow shift-click to transfer items to GUI
            // Minecraft will automatically handle the transfer
            if (event.isShiftClick()) {
                // Allow shift-click - item will move to GUI
                return;
            }
            // Allow regular clicks on player inventory
            return;
        }
    }

    /**
     * Handle inventory drag event for sell GUI
     */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        String title = event.getView().getTitle();
        
        // Check if this is the sell GUI
        if (!title.equals(SELL_GUI_TITLE)) {
            return;
        }

        // Allow dragging items into slots 0-44 of the GUI
        for (int slot : event.getRawSlots()) {
            // If dragging into GUI inventory (slots 0-53)
            if (slot < 54) {
                // Allow dragging into slots 0-44
                if (slot >= 0 && slot < 45) {
                    return;
                }
                // Cancel dragging into glass panels and close button
                if ((slot >= 45 && slot < 49) || (slot >= 50 && slot < 54) || slot == 49) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    /**
     * Handle inventory close event for sell GUI
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        String title = event.getView().getTitle();
        
        // Check if this is the sell GUI
        if (!title.equals(SELL_GUI_TITLE)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        Inventory inventory = event.getInventory();

        // Calculate total value and collect items
        double totalValue = 0;
        Map<Material, Integer> itemsToSell = new HashMap<>();

        // Iterate through slots 0-44 (the selling area)
        for (int i = 0; i < 45; i++) {
            ItemStack item = inventory.getItem(i);
            
            if (item != null && item.getType() != Material.AIR && !isGlassPane(item.getType())) {
                Material material = item.getType();
                int amount = item.getAmount();

                // Get sell price from SellingSystem
                double sellPrice = sellingSystem.getItemSellPrice(material);

                if (sellPrice > 0) {
                    totalValue += sellPrice * amount;
                    itemsToSell.put(material, itemsToSell.getOrDefault(material, 0) + amount);
                }
            }
        }

        // If there are items to sell, process the sale
        if (!itemsToSell.isEmpty()) {
            processSellGUI(player, itemsToSell, totalValue);
        } else {
            player.sendMessage("§7No items to sell!");
        }
    }

    /**
     * Process the sell GUI transaction
     */
    private void processSellGUI(Player player, Map<Material, Integer> itemsToSell, double totalValue) {
        // Deposit money to player
        boolean success = sellingSystem.depositMoney(player, totalValue);

        if (success) {
            // Send success message
            player.sendMessage("§a✅ Items sold successfully!");
            player.sendMessage("§7Total Earned: §a$" + sellingSystem.formatPrice(totalValue));
            player.sendMessage("§7New Balance: §a$" + sellingSystem.formatPrice(sellingSystem.getPlayerBalance(player)));

            // Log the transaction
            StringBuilder itemsLog = new StringBuilder();
            for (Map.Entry<Material, Integer> entry : itemsToSell.entrySet()) {
                if (itemsLog.length() > 0) {
                    itemsLog.append(", ");
                }
                itemsLog.append(entry.getValue()).append("x ").append(entry.getKey().name());
            }

            plugin.getLogger().info(
                player.getName() + " sold items via sell GUI: " + itemsLog.toString() + 
                " for $" + totalValue
            );
        } else {
            player.sendMessage("§c❌ Transaction failed!");
        }
    }

    /**
     * Create a glass pane item
     */
    private ItemStack createGlassPane() {
        ItemStack glassPane = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glassPane.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            glassPane.setItemMeta(meta);
        }
        return glassPane;
    }

    /**
     * Create a close button
     */
    private ItemStack createCloseButton() {
        ItemStack closeButton = new ItemStack(Material.BARRIER);
        ItemMeta meta = closeButton.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c✕ Close & Sell");
            closeButton.setItemMeta(meta);
        }
        return closeButton;
    }

    /**
     * Check if a material is a glass pane
     */
    private boolean isGlassPane(Material material) {
        return material == Material.LIGHT_GRAY_STAINED_GLASS_PANE || 
               material == Material.GLASS_PANE ||
               material == Material.WHITE_STAINED_GLASS_PANE ||
               material == Material.GRAY_STAINED_GLASS_PANE;
    }

    /**
     * Handle player drop item event for sell GUI
     */
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        
        // Check if player has sell GUI open
        if (player.getOpenInventory().getTitle().equals(SELL_GUI_TITLE)) {
            event.setCancelled(true);
            player.sendMessage("§c❌ You cannot drop items from the sell GUI!");
        }
    }
}
