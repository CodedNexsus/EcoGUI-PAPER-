package rohit.EcoGUI.shop;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class BuyingSystem {

    private JavaPlugin plugin;
    private Economy economy;

    public BuyingSystem(JavaPlugin plugin, Economy economy) {
        this.plugin = plugin;
        this.economy = economy;
    }

    /**
     * Process a buy transaction for a player
     * @param player The player making the purchase
     * @param shopItem The item being purchased
     * @param quantity The quantity to purchase
     * @return true if transaction was successful, false otherwise
     */
    public boolean processBuy(Player player, ShopItem shopItem, int quantity) {
        // Validate quantity
        if (quantity <= 0) {
            player.sendMessage("§c❌ Quantity must be greater than 0!");
            return false;
        }

        // Calculate total price
        double totalPrice = shopItem.getBuyPrice() * quantity;

        // Check if player has enough money
        if (!canAfford(player, totalPrice)) {
            double playerBalance = economy.getBalance(player);
            player.sendMessage("§c❌ Insufficient funds!");
            player.sendMessage("§7Need: §a$" + economy.format(totalPrice));
            player.sendMessage("§7Have: §c$" + economy.format(playerBalance));
            return false;
        }

        // Withdraw money from player
        EconomyResponse withdrawResponse = economy.withdrawPlayer(player, totalPrice);

        if (!withdrawResponse.transactionSuccess()) {
            player.sendMessage("§c❌ Transaction failed: " + withdrawResponse.errorMessage);
            return false;
        }

        // Give items to player
        ItemStack itemToGive = new ItemStack(shopItem.getMaterial(), quantity);
        player.getInventory().addItem(itemToGive);

        // Send success messages
        sendSuccessMessage(player, shopItem, quantity, totalPrice);

        // Log the transaction
        logTransaction(player, shopItem, quantity, totalPrice);

        return true;
    }

    /**
     * Check if player can afford an item
     * @param player The player
     * @param price The price to check
     * @return true if player has enough money
     */
    public boolean canAfford(Player player, double price) {
        double playerBalance = economy.getBalance(player);
        return playerBalance >= price;
    }

    /**
     * Get player's current balance
     * @param player The player
     * @return Player's balance
     */
    public double getPlayerBalance(Player player) {
        return economy.getBalance(player);
    }

    /**
     * Get formatted price string
     * @param price The price to format
     * @return Formatted price string
     */
    public String formatPrice(double price) {
        return economy.format(price);
    }

    /**
     * Calculate total price for an item
     * @param shopItem The shop item
     * @param quantity The quantity
     * @return Total price
     */
    public double calculateTotalPrice(ShopItem shopItem, int quantity) {
        return shopItem.getBuyPrice() * quantity;
    }

    /**
     * Send success message to player
     * @param player The player
     * @param shopItem The item purchased
     * @param quantity The quantity purchased
     * @param totalPrice The total price paid
     */
    private void sendSuccessMessage(Player player, ShopItem shopItem, int quantity, double totalPrice) {
        player.sendMessage("§a✅ Purchase successful!");
        player.sendMessage("§7Item: §e" + shopItem.getMaterial().name());
        player.sendMessage("§7Quantity: §e" + quantity);
        player.sendMessage("§7Total Cost: §c$" + economy.format(totalPrice));
        player.sendMessage("§7New Balance: §a$" + economy.format(economy.getBalance(player)));
    }

    /**
     * Log the transaction to console
     * @param player The player
     * @param shopItem The item purchased
     * @param quantity The quantity purchased
     * @param totalPrice The total price paid
     */
    private void logTransaction(Player player, ShopItem shopItem, int quantity, double totalPrice) {
        plugin.getLogger().info(
            player.getName() + " bought " + quantity + "x " + shopItem.getMaterial().name() + 
            " for $" + totalPrice
        );
    }

    /**
     * Deposit money to player (for refunds or other purposes)
     * @param player The player
     * @param amount The amount to deposit
     * @return true if deposit was successful
     */
    public boolean depositMoney(Player player, double amount) {
        EconomyResponse response = economy.depositPlayer(player, amount);
        return response.transactionSuccess();
    }

    /**
     * Withdraw money from player
     * @param player The player
     * @param amount The amount to withdraw
     * @return true if withdrawal was successful
     */
    public boolean withdrawMoney(Player player, double amount) {
        EconomyResponse response = economy.withdrawPlayer(player, amount);
        return response.transactionSuccess();
    }
}
