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

public class SellGUIListener implements Listener {

    private static final int GUI_SIZE = 54;
    private static final int ITEM_AREA_END = 44;
    private static final int CONTROL_ROW_START = 45;
    private static final int CLOSE_BUTTON_SLOT = 49;
    private static final String SELL_GUI_TITLE = "§6Sell Items";

    private final Main plugin;
    private final SellingSystem sellingSystem;

    public SellGUIListener(Main plugin) {
        this.plugin = plugin;
        this.sellingSystem = new SellingSystem(plugin, plugin.getEconomy());
    }

    public void openSellGUI(Player player) {
        Inventory sellInventory = Bukkit.createInventory(new SellInventoryHolder(null), GUI_SIZE, SELL_GUI_TITLE);

        ItemStack pane = createGlassPane();
        for (int i = CONTROL_ROW_START; i < GUI_SIZE; i++) {
            if (i == CLOSE_BUTTON_SLOT) continue;
            sellInventory.setItem(i, pane);
        }
        sellInventory.setItem(CLOSE_BUTTON_SLOT, createCloseButton());

        player.openInventory(sellInventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryView view = event.getView();
        if (!isSellGui(view)) return;

        Player player = (Player) event.getWhoClicked();
        Inventory clicked = event.getClickedInventory();
        if (clicked == null) return;

        Inventory top = view.getTopInventory();

        if (clicked.equals(top)) {
            int slot = event.getSlot();

            if (slot >= 0 && slot <= ITEM_AREA_END) {
                return;
            }

            event.setCancelled(true);
            if (slot == CLOSE_BUTTON_SLOT) {
                player.closeInventory();
            }
            return;
        }

    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!isSellGui(event.getView())) return;

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

        Map<Material, Integer> soldCounts = new HashMap<>();
        List<ItemStack> refundItems = new ArrayList<>();
        List<ItemStack> allItemsForFailRefund = new ArrayList<>();
        double totalValue = 0D;

        for (int i = 0; i <= ITEM_AREA_END; i++) {
            ItemStack it = inv.getItem(i);
            if (it == null || it.getType() == Material.AIR) continue;

            allItemsForFailRefund.add(it.clone());

            double unitSell = sellingSystem.getItemSellPrice(it.getType());
            if (unitSell > 0) {
                soldCounts.merge(it.getType(), it.getAmount(), Integer::sum);
                totalValue += unitSell * it.getAmount();
            } else {
                refundItems.add(it.clone());
            }
        }

        if (soldCounts.isEmpty() && refundItems.isEmpty()) {
            player.sendMessage("§7No items to sell!");
            return;
        }

        boolean depositOk = true;
        if (totalValue > 0) {
            depositOk = sellingSystem.depositMoney(player, totalValue);
        }

        if (!depositOk) {
            for (ItemStack stack : allItemsForFailRefund) {
                addOrDrop(player, stack);
            }
            player.sendMessage("§c❌ Transaction failed!");
            clearItemArea(inv);
            return;
        }

        for (ItemStack stack : refundItems) {
            addOrDrop(player, stack);
        }

        clearItemArea(inv);

        if (totalValue > 0) {
            player.sendMessage("§a✅ Items sold successfully!");
            player.sendMessage("§7Total Earned: §a" + sellingSystem.formatPrice(totalValue));
            player.sendMessage("§7New Balance: §a" + sellingSystem.formatPrice(sellingSystem.getPlayerBalance(player)));

            StringBuilder sb = new StringBuilder();
            for (Map.Entry<Material, Integer> e : soldCounts.entrySet()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(e.getValue()).append("x ").append(e.getKey().name());
            }
            plugin.getLogger().info(player.getName() + " sold via Sell GUI: " + sb + " for " + totalValue);
        } else {
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
