package rohit.EcoGUI.config;

import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public class ConfigManager {

    private JavaPlugin plugin;
    private File pluginFolder;
    private File sectionsFolder;
    private File shopsFolder;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.pluginFolder = plugin.getDataFolder();
        this.sectionsFolder = new File(pluginFolder, "sections");
        this.shopsFolder = new File(pluginFolder, "shops");
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
