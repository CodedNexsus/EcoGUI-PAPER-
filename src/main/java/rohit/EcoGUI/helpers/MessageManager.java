package rohit.EcoGUI.helpers;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import rohit.EcoGUI.Main;
import rohit.EcoGUI.config.ConfigManager;

import java.io.File;

public class MessageManager {

    private ConfigManager configManager;
    private FileConfiguration messagesConfig;

    public MessageManager(ConfigManager configManager) {
        this.configManager = configManager;
        loadMessages();
    }

    private void loadMessages() {
        Main plugin = (Main) configManager.getPlugin();
        File messagesFile = new File(plugin.getDataFolder(), "msg.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("msg.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public String getMessage(String key) {
        return messagesConfig.getString(key, "&cMessage not found: " + key);
    }

    public void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    public void sendPurchaseMessage(CommandSender sender, String itemName, int quantity, double totalPrice) {
        if (configManager.isEnablePurchaseMessages()) {
            String message = getMessage("purchase_message")
                    .replace("{quantity}", String.valueOf(quantity))
                    .replace("{item_name}", itemName)
                    .replace("{total_price}", String.valueOf(totalPrice));
            sendMessage(sender, message);
        }
    }

    public void sendSellMessage(CommandSender sender, String itemName, int quantity, double totalPrice) {
        if (configManager.isEnableSellMessages()) {
            String message = getMessage("sell_message")
                    .replace("{quantity}", String.valueOf(quantity))
                    .replace("{item_name}", itemName)
                    .replace("{total_price}", String.valueOf(totalPrice));
            sendMessage(sender, message);
        }
    }

    public void sendInventoryFullWarning(CommandSender sender) {
        if (configManager.isEnableInventoryFullWarnings()) {
            sendMessage(sender, getMessage("inventory_full_warning"));
        }
    }

    public void sendDebugMessage(String message) {
        if (configManager.isDebugLoggingEnabled()) {
            System.out.println("[EcoGUI Debug] " + message);
        }
    }

    public void logTransaction(String message) {
        if (configManager.isLogTransactionsEnabled()) {
            System.out.println("[EcoGUI Transaction] " + message);
        }
    }

    public void reloadMessages() {
        Main plugin = (Main) configManager.getPlugin();
        File messagesFile = new File(plugin.getDataFolder(), "msg.yml");
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }
}
