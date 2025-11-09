package rohit.EcoGUI.shop;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import rohit.EcoGUI.Main;
import rohit.EcoGUI.helpers.MessageManager;

import java.io.File;
import java.util.Map;
import java.util.Set;

public class SellingSystem {

    private JavaPlugin plugin;
    private Economy economy;
    private ShopManager shopManager;
    private ConfigManager configManager;
    private MessageManager messageManager;

    public SellingSystem(JavaPlugin plugin, Economy economy) {
        this.plugin = plugin;
        this.economy = economy;
        if (plugin instanceof Main) {
            Main mainPlugin = (Main) plugin;
            this.shopManager = mainPlugin.getShopManager();
            this.configManager = mainPlugin.getConfigManager();
            this.messageManager = mainPlugin.getMessageManager();
        }
    }

    public boolean processSell(Player player, Material material, int amount) {
        if (amount <= 0) {
            messageManager.sendMessage(player, "&cAmount must be greater than 0!");
            return false;
        }
        
        // Check min and max sell quantity from config
        if (plugin instanceof Main) {
            Main mainPlugin = (Main) plugin;
            int minSellQuantity = mainPlugin.getConfigManager().getMinSellQuantity();
            int maxSellQuantity = mainPlugin.getConfigManager().getMaxSellQuantity();
            
            // Check minimum quantity
            if (minSellQuantity > 0 && amount < minSellQuantity) {
                messageManager.sendMessage(player, "&cMinimum sell quantity is " + minSellQuantity + "!");
                messageManager.sendMessage(player, "&7You tried to sell: &e" + amount);
                return false;
            }
            
            // Check maximum quantity
            if (maxSellQuantity > 0 && amount > maxSellQuantity) {
                messageManager.sendMessage(player, "&cMaximum sell quantity is " + maxSellQuantity + "!");
                messageManager.sendMessage(player, "&7You tried to sell: &e" + amount);
                return false;
            }
        }

        int itemCount = countItemInInventory(player, material);
        if (itemCount < amount) {
            messageManager.sendMessage(player, "&cInsufficient items!");
            messageManager.sendMessage(player, "&7Need: &e" + amount);
            messageManager.sendMessage(player, "&7Have: &c" + itemCount);
            return false;
        }

        double sellPrice = getItemSellPrice(material);
        
        if (sellPrice < 0) {
            messageManager.sendMessage(player, "&cThis item cannot be sold!");
            return false;
        }

        if (sellPrice == 0) {
            messageManager.sendMessage(player, "&cThis item is not configured for selling!");
            return false;
        }

        double totalPrice = sellPrice * amount;

        removeItemFromInventory(player, material, amount);

        EconomyResponse response = economy.depositPlayer(player, totalPrice);

        if (!response.transactionSuccess()) {
            player.getInventory().addItem(new ItemStack(material, amount));
            messageManager.sendMessage(player, "&cTransaction failed: " + response.errorMessage);
            return false;
        }

        sendSuccessMessage(player, material, amount, totalPrice, sellPrice);

        logTransaction(player, material, amount, totalPrice);

        return true;
    }

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

        File[] shopFiles = shopsFolder.listFiles((dir, name) -> name.endsWith(".yml"));

        if (shopFiles == null || shopFiles.length == 0) {
            return 0;
        }

        for (File shopFile : shopFiles) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(shopFile);
            
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
                            
                            if (configMaterial.equalsIgnoreCase(materialName)) {
                                double sellPrice = config.getDouble(sellPath, 0);
                                
                                if (sellPrice > 0) {
                                    return sellPrice;
                                } else if (sellPrice == -1.0) {
                                    return -1;
                                }
                            }
                        }
                    }
                }
            }
        }

        return 0;
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

    private void sendSuccessMessage(Player player, Material material, int amount, double totalPrice, double unitPrice) {
        messageManager.sendSellMessage(player, material.name(), amount, totalPrice);
    }

    private void logTransaction(Player player, Material material, int amount, double totalPrice) {
        messageManager.logTransaction(player.getName() + " sold " + amount + "x " + material.name() + " for $" + totalPrice);
    }

    public double getPlayerBalance(Player player) {
        return economy.getBalance(player);
    }

    public String formatPrice(double price) {
        return economy.format(price);
    }

    public boolean depositMoney(Player player, double amount) {
        EconomyResponse response = economy.depositPlayer(player, amount);
        return response.transactionSuccess();
    }
}
