package rohit.EcoGUI.shop;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import rohit.EcoGUI.Main;
import rohit.EcoGUI.config.ConfigManager;

import java.io.File;
import java.util.Map;
import java.util.Set;

public class SellingSystem {

    private JavaPlugin plugin;
    private Economy economy;
    private ShopManager shopManager;
    private ConfigManager configManager;

    public SellingSystem(JavaPlugin plugin, Economy economy) {
        this.plugin = plugin;
        this.economy = economy;
        if (plugin instanceof Main) {
            Main mainPlugin = (Main) plugin;
            this.shopManager = mainPlugin.getShopManager();
            this.configManager = mainPlugin.getConfigManager();
        }
    }

    /**
     * Process a sell transaction for a player
     * @param player The player selling items
     * @param material The material being sold
     * @param amount The amount of items to sell
     * @return true if transaction was successful, false otherwise
     */
    public boolean processSell(Player player, Material material, int amount) {
        // Validate inputs
        if (amount <= 0) {
            player.sendMessage("§c❌ Amount must be greater than 0!");
            return false;
        }

        // Check if player has the item
        int itemCount = countItemInInventory(player, material);
        if (itemCount < amount) {
            player.sendMessage("§c❌ Insufficient items!");
            player.sendMessage("§7Need: §e" + amount);
            player.sendMessage("§7Have: §c" + itemCount);
            return false;
        }

        // Get the sell price from shop configuration
        double sellPrice = getItemSellPrice(material);
        
        if (sellPrice < 0) {
            player.sendMessage("§c❌ This item cannot be sold!");
            return false;
        }

        if (sellPrice == 0) {
            player.sendMessage("§c❌ This item is not configured for selling!");
            return false;
        }

        // Calculate total price
        double totalPrice = sellPrice * amount;

        // Remove items from inventory
        removeItemFromInventory(player, material, amount);

        // Deposit money to player
        EconomyResponse response = economy.depositPlayer(player, totalPrice);

        if (!response.transactionSuccess()) {
            // Refund items if transaction failed
            player.getInventory().addItem(new ItemStack(material, amount));
            player.sendMessage("§c❌ Transaction failed: " + response.errorMessage);
            return false;
        }

        // Send success message
        sendSuccessMessage(player, material, amount, totalPrice, sellPrice);

        // Log the transaction
        logTransaction(player, material, amount, totalPrice);

        return true;
    }

    /**
     * Get the sell price of an item from the shop configuration
     * @param material The material to get the price for
     * @return The sell price, or -1 if the item cannot be sold, or 0 if not configured
     */
    public double getItemSellPrice(Material material) {
        if (shopManager == null || configManager == null) {
            plugin.getLogger().warning("⚠️ ShopManager or ConfigManager not available!");
            return 0;
        }

        String materialName = material.name();
        File shopsFolder = configManager.getShopsFolder();

        if (!shopsFolder.exists()) {
            return 0;
        }

        // Search through all shop files to find the item and its sell price
        File[] shopFiles = shopsFolder.listFiles((dir, name) -> name.endsWith(".yml"));

        if (shopFiles == null || shopFiles.length == 0) {
            return 0;
        }

        for (File shopFile : shopFiles) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(shopFile);
            
            // Get all pages in this shop
            Set<String> pages = config.getKeys(false);
            for (String pageName : pages) {
                if (config.isConfigurationSection(pageName)) {
                    String itemsPath = pageName + ".items";
                    if (config.isConfigurationSection(itemsPath)) {
                        Set<String> itemSlots = config.getConfigurationSection(itemsPath).getKeys(false);
                        
                        for (String slot : itemSlots) {
                            String materialPath = itemsPath + "." + slot + ".material";
                            String sellPath = itemsPath + "." + slot + ".sell";
                            
                            String configMaterial = config.getString(materialPath, "");
                            
                            // Check if this is the material we're looking for
                            if (configMaterial.equalsIgnoreCase(materialName)) {
                                double sellPrice = config.getDouble(sellPath, 0);
                                
                                // Return the sell price if it's valid
                                if (sellPrice > 0) {
                                    return sellPrice;
                                } else if (sellPrice == -1.0) {
                                    // -1.0 means item cannot be sold
                                    return -1;
                                }
                            }
                        }
                    }
                }
            }
        }

        // Item not found in any shop configuration
        return 0;
    }

    /**
     * Count how many of a specific material the player has in their inventory
     */
    private int countItemInInventory(Player player, Material material) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }

    /**
     * Remove items from player's inventory
     */
    private void removeItemFromInventory(Player player, Material material, int amount) {
        int remaining = amount;
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

    /**
     * Send success message to player
     */
    private void sendSuccessMessage(Player player, Material material, int amount, double totalPrice, double unitPrice) {
        player.sendMessage("§a✅ Sale successful!");
        player.sendMessage("§7Item: §e" + material.name());
        player.sendMessage("§7Quantity: §e" + amount);
        player.sendMessage("§7Unit Price: §a$" + economy.format(unitPrice));
        player.sendMessage("§7Total Earned: §a$" + economy.format(totalPrice));
        player.sendMessage("§7New Balance: §a$" + economy.format(economy.getBalance(player)));
    }

    /**
     * Log the transaction to console
     */
    private void logTransaction(Player player, Material material, int amount, double totalPrice) {
        plugin.getLogger().info(
            player.getName() + " sold " + amount + "x " + material.name() + 
            " for $" + totalPrice
        );
    }

    /**
     * Get player's current balance
     */
    public double getPlayerBalance(Player player) {
        return economy.getBalance(player);
    }

    /**
     * Get formatted price string
     */
    public String formatPrice(double price) {
        return economy.format(price);
    }

    /**
     * Deposit money to player's account
     * @param player The player to deposit money to
     * @param amount The amount to deposit
     * @return true if transaction was successful, false otherwise
     */
    public boolean depositMoney(Player player, double amount) {
        EconomyResponse response = economy.depositPlayer(player, amount);
        return response.transactionSuccess();
    }
}
