package rohit.EcoGUI.shop;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ShopManager {

    private JavaPlugin plugin;
    private File shopsFolder;
    private Map<String, Shop> shops;

    public ShopManager(JavaPlugin plugin, File shopsFolder) {
        this.plugin = plugin;
        this.shopsFolder = shopsFolder;
        this.shops = new HashMap<>();
    }

    public void loadShops() {
        shops.clear();

        if (!shopsFolder.exists()) {
            plugin.getLogger().warning("⚠️ Shops folder does not exist!");
            return;
        }

        File[] files = shopsFolder.listFiles((dir, name) -> name.endsWith(".yml"));

        if (files == null || files.length == 0) {
            plugin.getLogger().info("📂 No shop files found in shops folder");
            return;
        }

        for (File file : files) {
            loadShop(file);
        }

        plugin.getLogger().info("✅ Loaded " + shops.size() + " shops");
    }

    private void loadShop(File file) {
        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            String shopName = file.getName().replace(".yml", "");
            Shop shop = new Shop(shopName);

            Set<String> pages = config.getKeys(false);
            for (String pageName : pages) {
                if (config.isConfigurationSection(pageName)) {
                    Map<Integer, ShopItem> pageItems = loadPageItems(config, pageName);
                    shop.addPage(pageName, pageItems);
                }
            }

            shops.put(shopName, shop);
            plugin.getLogger().info("✅ Loaded shop: " + shopName);

        } catch (Exception e) {
            plugin.getLogger().warning("❌ Error loading shop file: " + file.getName());
            e.printStackTrace();
        }
    }

    private Map<Integer, ShopItem> loadPageItems(YamlConfiguration config, String pageName) {
        Map<Integer, ShopItem> items = new HashMap<>();

        String itemsPath = pageName + ".items";
        if (!config.isConfigurationSection(itemsPath)) {
            return items;
        }

        Set<String> itemKeys = config.getConfigurationSection(itemsPath).getKeys(false);
        for (String itemKey : itemKeys) {
            try {
                int slot = Integer.parseInt(itemKey);
                String materialPath = itemsPath + "." + itemKey + ".material";
                String buyPath = itemsPath + "." + itemKey + ".buy";
                String sellPath = itemsPath + "." + itemKey + ".sell";

                String materialName = config.getString(materialPath, "STONE");
                double buyPrice = config.getDouble(buyPath, 0);
                double sellPrice = config.getDouble(sellPath, 0);

                Material material;
                try {
                    material = Material.valueOf(materialName.toUpperCase());
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("⚠️ Invalid material '" + materialName + "' in shop: " + pageName);
                    material = Material.STONE;
                }

                ShopItem shopItem = new ShopItem(material, buyPrice, sellPrice, slot);
                items.put(slot, shopItem);

            } catch (NumberFormatException e) {
                plugin.getLogger().warning("⚠️ Invalid slot number: " + itemKey);
            }
        }

        return items;
    }

    public Shop getShop(String name) {
        return shops.get(name);
    }

    public Map<String, Shop> getAllShops() {
        return shops;
    }

    public int getTotalShops() {
        return shops.size();
    }
}
