package rohit.EcoGUI.shop;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class BuyTransactionManager {

    private JavaPlugin plugin;
    private Economy economy;

    public BuyTransactionManager(JavaPlugin plugin, Economy economy) {
        this.plugin = plugin;
        this.economy = economy;
    }

    public boolean processBuy(Player player, ShopItem shopItem, int quantity) {
        if (quantity <= 0) {
            player.sendMessage("§c❌ Quantity must be greater than 0!");
            return false;
        }

        double totalPrice = shopItem.getBuyPrice() * quantity;
        double playerBalance = economy.getBalance(player);

        if (playerBalance < totalPrice) {
            player.sendMessage("§c❌ Insufficient funds!");
            player.sendMessage("§7Need: §a$" + economy.format(totalPrice));
            player.sendMessage("§7Have: §c$" + economy.format(playerBalance));
            return false;
        }

        EconomyResponse response = economy.withdrawPlayer(player, totalPrice);

        if (!response.transactionSuccess()) {
            player.sendMessage("§c❌ Transaction failed: " + response.errorMessage);
            return false;
        }

        ItemStack itemToGive = new ItemStack(shopItem.getMaterial(), quantity);
        player.getInventory().addItem(itemToGive);

        player.sendMessage("§a✅ Purchase successful!");
        player.sendMessage("§7Item: §e" + shopItem.getMaterial().name());
        player.sendMessage("§7Quantity: §e" + quantity);
        player.sendMessage("§7Total Cost: §c$" + economy.format(totalPrice));
        player.sendMessage("§7New Balance: §a$" + economy.format(economy.getBalance(player)));

        plugin.getLogger().info(player.getName() + " bought " + quantity + "x " + shopItem.getMaterial().name() + " for $" + totalPrice);

        return true;
    }

    public boolean canAfford(Player player, ShopItem shopItem, int quantity) {
        double totalPrice = shopItem.getBuyPrice() * quantity;
        double playerBalance = economy.getBalance(player);
        return playerBalance >= totalPrice;
    }

    public double getTotalPrice(ShopItem shopItem, int quantity) {
        return shopItem.getBuyPrice() * quantity;
    }

    public String getFormattedPrice(double price) {
        return economy.format(price);
    }
}
