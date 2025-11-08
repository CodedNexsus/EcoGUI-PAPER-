package rohit.EcoGUI.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private JavaPlugin plugin;
    private File pluginFolder;
    private File sectionsFolder;
    private File shopsFolder;
    private FileConfiguration config;
    private File configFile;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.pluginFolder = plugin.getDataFolder();
        this.sectionsFolder = new File(pluginFolder, "sections");
        this.shopsFolder = new File(pluginFolder, "shops");
        this.configFile = new File(pluginFolder, "config.yml");
    }

    public void loadOrCreateFolders() {
        if (pluginFolder.exists()) {
            plugin.getLogger().info("📂 Loading data from EcoGUI folder");
        } else {
            plugin.getLogger().info("📁 Creating EcoGUI folder");
            pluginFolder.mkdirs();
        }

        if (sectionsFolder.exists()) {
            plugin.getLogger().info("📂 Loading sections from sections folder");
        } else {
            plugin.getLogger().info("📁 Creating sections folder");
            sectionsFolder.mkdirs();
        }

        if (shopsFolder.exists()) {
            plugin.getLogger().info("📂 Loading shops from shops folder");
        } else {
            plugin.getLogger().info("📁 Creating shops folder");
            shopsFolder.mkdirs();
        }
        
        loadConfig();
    }
    
    private void loadConfig() {
        if (!configFile.exists()) {
            plugin.getLogger().info("📁 Creating config.yml");
            plugin.saveResource("config.yml", false);
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);
        plugin.getLogger().info("✅ Configuration loaded successfully");
    }
    
    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
        plugin.getLogger().info("✅ Configuration reloaded");
    }
    
    public FileConfiguration getConfig() {
        if (config == null) {
            loadConfig();
        }
        return config;
    }
    
    public int getMaxBuyQuantity() {
        return getConfig().getInt("purchase.max_buy_quantity", 9999);
    }
    
    public int getMinBuyQuantity() {
        return getConfig().getInt("purchase.min_buy_quantity", 1);
    }
    
    public int getMaxSellQuantity() {
        return getConfig().getInt("selling.max_sell_quantity", 9999);
    }
    
    public int getMinSellQuantity() {
        return getConfig().getInt("selling.min_sell_quantity", 1);
    }
    
    public boolean isEnableSellGui() {
        return getConfig().getBoolean("inventory.enable_sell_gui", true);
    }
    
    public boolean isEnableBuyGui() {
        return getConfig().getBoolean("inventory.enable_buy_gui", true);
    }
    
    public int getItemsPerPage() {
        return getConfig().getInt("pagination.items_per_page", 45);
    }
    
    public boolean isEnablePagination() {
        return getConfig().getBoolean("pagination.enable_pagination", true);
    }
    
    public boolean isEnablePurchaseMessages() {
        return getConfig().getBoolean("messages.enable_purchase_messages", true);
    }
    
    public boolean isEnableSellMessages() {
        return getConfig().getBoolean("messages.enable_sell_messages", true);
    }
    
    public boolean isEnableInventoryFullWarnings() {
        return getConfig().getBoolean("messages.enable_inventory_full_warnings", true);
    }
    
    public boolean isDebugLoggingEnabled() {
        return getConfig().getBoolean("debug.enable_debug_logging", false);
    }
    
    public boolean isLogTransactionsEnabled() {
        return getConfig().getBoolean("debug.log_transactions", true);
    }

    public File getPluginFolder() {
        return pluginFolder;
    }

    public File getSectionsFolder() {
        return sectionsFolder;
    }

    public File getShopsFolder() {
        return shopsFolder;
    }

    public File getSectionFolder(String sectionName) {
        return new File(sectionsFolder, sectionName);
    }

    public File getShopFolder(String shopName) {
        return new File(shopsFolder, shopName);
    }

    public void createSectionFolder(String sectionName) {
        File sectionFolder = getSectionFolder(sectionName);
        if (sectionFolder.exists()) {
            plugin.getLogger().info("📂 Loading section: " + sectionName);
        } else {
            plugin.getLogger().info("📁 Creating section folder: " + sectionName);
            sectionFolder.mkdirs();
        }
    }

    public void createShopFolder(String shopName) {
        File shopFolder = getShopFolder(shopName);
        if (shopFolder.exists()) {
            plugin.getLogger().info("📂 Loading shop: " + shopName);
        } else {
            plugin.getLogger().info("📁 Creating shop folder: " + shopName);
            shopFolder.mkdirs();
        }
    }
}
