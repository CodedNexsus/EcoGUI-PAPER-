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

    public boolean processSell(Player player, Material material, int amount) {
        if (amount <= 0) {
            player.sendMessage("§c❌ Amount must be greater than 0!");
            return false;
        }

        int itemCount = countItemInInventory(player, material);
        if (itemCount < amount) {
            player.sendMessage("§c❌ Insufficient items!");
            player.sendMessage("§7Need: §e" + amount);
            player.sendMessage("§7Have: §c" + itemCount);
            return false;
        }

        double sellPrice = getItemSellPrice(material);
        
        if (sellPrice < 0) {
            player.sendMessage("§c❌ This item cannot be sold!");
            return false;
        }

        if (sellPrice == 0) {
            player.sendMessage("§c❌ This item is not configured for selling!");
            return false;
        }

        double totalPrice = sellPrice * amount;

        removeItemFromInventory(player, material, amount);

        EconomyResponse response = economy.depositPlayer(player, totalPrice);

        if (!response.transactionSuccess()) {
            player.getInventory().addItem(new ItemStack(material, amount));
            player.sendMessage("§c❌ Transaction failed: " + response.errorMessage);
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
        player.sendMessage("§a✅ Sale successful!");
        player.sendMessage("§7Item: §e" + material.name());
        player.sendMessage("§7Quantity: §e" + amount);
        player.sendMessage("§7Unit Price: §a$" + economy.format(unitPrice));
        player.sendMessage("§7Total Earned: §a$" + economy.format(totalPrice));
        player.sendMessage("§7New Balance: §a$" + economy.format(economy.getBalance(player)));
    }

    private void logTransaction(Player player, Material material, int amount, double totalPrice) {
        plugin.getLogger().info(
            player.getName() + " sold " + amount + "x " + material.name() + 
            " for $" + totalPrice
        );
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
