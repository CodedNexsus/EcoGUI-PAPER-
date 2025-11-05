package rohit.EcoGUI.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import rohit.EcoGUI.Main;
import rohit.EcoGUI.shop.SellingSystem;

import java.util.HashMap;
import java.util.Map;

public class SellAllCommand implements CommandExecutor {

    private Main plugin;
    private SellingSystem sellingSystem;

    public SellAllCommand(Main plugin) {
        this.plugin = plugin;
        this.sellingSystem = new SellingSystem(plugin, plugin.getEconomy());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        Map<Material, Integer> itemsToSell = new HashMap<>();
        double totalValue = 0;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                Material material = item.getType();
                int amount = item.getAmount();

                double sellPrice = sellingSystem.getItemSellPrice(material);

                if (sellPrice > 0) {
                    totalValue += sellPrice * amount;
                    itemsToSell.put(material, itemsToSell.getOrDefault(material, 0) + amount);
                }
            }
        }

        if (itemsToSell.isEmpty()) {
            player.sendMessage("§c❌ You don't have any items to sell!");
            return true;
        }

        processSellAll(player, itemsToSell, totalValue);

        return true;
    }

    private void processSellAll(Player player, Map<Material, Integer> itemsToSell, double totalValue) {
        removeItemsFromInventory(player, itemsToSell);

        boolean success = sellingSystem.depositMoney(player, totalValue);

        if (success) {
            player.sendMessage("§a✅ All items sold successfully!");
            player.sendMessage("§7Items Sold: §e" + itemsToSell.size());
            player.sendMessage("§7Total Earned: §a$" + sellingSystem.formatPrice(totalValue));
            player.sendMessage("§7New Balance: §a$" + sellingSystem.formatPrice(sellingSystem.getPlayerBalance(player)));

            StringBuilder itemsLog = new StringBuilder();
            for (Map.Entry<Material, Integer> entry : itemsToSell.entrySet()) {
                if (itemsLog.length() > 0) {
                    itemsLog.append(", ");
                }
                itemsLog.append(entry.getValue()).append("x ").append(entry.getKey().name());
            }

            plugin.getLogger().info(
                player.getName() + " sold all items: " + itemsLog.toString() + 
                " for $" + totalValue
            );
        } else {
            for (Map.Entry<Material, Integer> entry : itemsToSell.entrySet()) {
                player.getInventory().addItem(new ItemStack(entry.getKey(), entry.getValue()));
            }
            player.sendMessage("§c❌ Transaction failed!");
        }
    }

    private void removeItemsFromInventory(Player player, Map<Material, Integer> itemsToSell) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                Material material = item.getType();
                
                if (itemsToSell.containsKey(material)) {
                    double sellPrice = sellingSystem.getItemSellPrice(material);
                    
                    if (sellPrice > 0) {
                        item.setAmount(0);
                    }
                }
            }
        }
    }
}
