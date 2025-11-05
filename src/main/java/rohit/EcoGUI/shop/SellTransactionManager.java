package rohit.EcoGUI.shop;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class SellTransactionManager {

    private JavaPlugin plugin;
    private Economy economy;

    public SellTransactionManager(JavaPlugin plugin, Economy economy) {
        this.plugin = plugin;
        this.economy = economy;
    }

    public boolean processSell(Player player, ShopItem shopItem, int quantity) {
        if (quantity <= 0) {
            player.sendMessage("§c❌ Quantity must be greater than 0!");
            return false;
        }

        if (shopItem.getSellPrice() == -1.0) {
            player.sendMessage("§c❌ This item cannot be sold!");
            return false;
        }

        int itemCount = countItemInInventory(player, shopItem.getMaterial());
        if (itemCount < quantity) {
            player.sendMessage("§c❌ Insufficient items!");
            player.sendMessage("§7Need: §e" + quantity);
            player.sendMessage("§7Have: §c" + itemCount);
            return false;
        }

        double totalPrice = shopItem.getSellPrice() * quantity;
        EconomyResponse response = economy.depositPlayer(player, totalPrice);

        if (!response.transactionSuccess()) {
            player.sendMessage("§c❌ Transaction failed: " + response.errorMessage);
            return false;
        }

        removeItemFromInventory(player, shopItem.getMaterial(), quantity);

        player.sendMessage("§a✅ Sale successful!");
        player.sendMessage("§7Item: §e" + shopItem.getMaterial().name());
        player.sendMessage("§7Quantity: §e" + quantity);
        player.sendMessage("§7Total Earned: §a$" + economy.format(totalPrice));
        player.sendMessage("§7New Balance: §a$" + economy.format(economy.getBalance(player)));

        plugin.getLogger().info(player.getName() + " sold " + quantity + "x " + shopItem.getMaterial().name() + " for $" + totalPrice);

        return true;
    }

    public boolean canSell(Player player, ShopItem shopItem, int quantity) {
        if (shopItem.getSellPrice() == -1.0) {
            return false;
        }
        int itemCount = countItemInInventory(player, shopItem.getMaterial());
        return itemCount >= quantity;
    }

    public int getItemCount(Player player, Material material) {
        return countItemInInventory(player, material);
    }

    public double getTotalPrice(ShopItem shopItem, int quantity) {
        if (shopItem.getSellPrice() == -1.0) {
            return 0;
        }
        return shopItem.getSellPrice() * quantity;
    }

    public String getFormattedPrice(double price) {
        return economy.format(price);
    }

    private int countItemInInventory(Player player, Material material) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }

    private void removeItemFromInventory(Player player, Material material, int quantity) {
        int remaining = quantity;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material && remaining > 0) {
                if (item.getAmount() <= remaining) {
                    remaining -= item.getAmount();
                    item.setAmount(0);
                } else {
                    item.setAmount(item.getAmount() - remaining);
                    remaining = 0;
                }
            }
        }
    }
}
