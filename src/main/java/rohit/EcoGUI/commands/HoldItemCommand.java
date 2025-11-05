package rohit.EcoGUI.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import rohit.EcoGUI.Main;
import rohit.EcoGUI.section.Section;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class HoldItemCommand implements CommandExecutor {

    private Main plugin;

    public HoldItemCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.isOp() && !player.hasPermission("ecogui.admin")) {
            player.sendMessage("§c❌ You don't have permission to use this command!");
            return true;
        }

        if (args.length < 3) {
            player.sendMessage("§c❌ Usage: /hitem <section name> <buy price> <sell price>");
            return true;
        }

        String sectionName = args[0];
        String buyPriceStr = args[1];
        String sellPriceStr = args[2];

        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand == null || itemInHand.getType() == Material.AIR) {
            player.sendMessage("§c❌ You must be holding an item!");
            return true;
        }

        Section section = plugin.getSectionManager().getSection(sectionName);
        if (section == null) {
            player.sendMessage("§c❌ Section not found: " + sectionName);
            return true;
        }

        if (!section.isEnabled()) {
            player.sendMessage("§c❌ Section is disabled: " + sectionName);
            return true;
        }

        File shopFile = new File(plugin.getConfigManager().getShopsFolder(), sectionName + ".yml");
        if (!shopFile.exists()) {
            player.sendMessage("§c❌ Shop file does not exist for section: " + sectionName);
            player.sendMessage("§7Create the shop file first or use /csection to create the section.");
            return true;
        }

        double buyPrice;
        double sellPrice;

        try {
            buyPrice = Double.parseDouble(buyPriceStr);
            sellPrice = Double.parseDouble(sellPriceStr);
        } catch (NumberFormatException e) {
            player.sendMessage("§c❌ Buy price and sell price must be numbers!");
            return true;
        }

        if (buyPrice < 0 || sellPrice < 0) {
            player.sendMessage("§c❌ Prices cannot be negative!");
            return true;
        }

        if (sellPrice == -1.0) {
            player.sendMessage("§c❌ Sell price -1.0 is reserved for non-sellable items!");
            player.sendMessage("§7Use a different price or contact an admin to mark as non-sellable.");
            return true;
        }

        Material material = itemInHand.getType();
        String materialName = material.name();

        String pageName = getNextAvailablePage(sectionName);
        int nextSlot = getNextAvailableSlot(sectionName, pageName);

        if (nextSlot == -1) {
            player.sendMessage("§c❌ No available slots in this section!");
            return true;
        }

        saveItemToFile(sectionName, pageName, nextSlot, materialName, buyPrice, sellPrice);

        player.sendMessage("§a✅ Item added successfully!");
        player.sendMessage("§7Section: §e" + sectionName);
        player.sendMessage("§7Page: §e" + pageName);
        player.sendMessage("§7Material: §e" + materialName);
        player.sendMessage("§7Slot: §e" + nextSlot);
        player.sendMessage("§7Buy Price: §a$" + buyPrice);
        player.sendMessage("§7Sell Price: §c$" + sellPrice);
        player.sendMessage("");
        player.sendMessage("§6💡 Run §e/rshop §6to reload and see changes!");

        plugin.getLogger().info("✅ Item added by " + player.getName() + " to section " + sectionName + " page " + pageName + ": " + materialName + " at slot " + nextSlot + " (Buy: $" + buyPrice + ", Sell: $" + sellPrice + ")");

        return true;
    }

    private String getNextAvailablePage(String sectionName) {
        File shopFile = new File(plugin.getConfigManager().getShopsFolder(), sectionName + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(shopFile);

        int pageNumber = 1;
        while (true) {
            String pageName = "page" + pageNumber;
            String itemsPath = pageName + ".items";

            if (!config.isConfigurationSection(itemsPath)) {
                return pageName;
            }

            Set<String> itemKeys = config.getConfigurationSection(itemsPath).getKeys(false);
            if (itemKeys.size() < 45) {
                return pageName;
            }

            pageNumber++;
        }
    }

    private int getNextAvailableSlot(String sectionName, String pageName) {
        File shopFile = new File(plugin.getConfigManager().getShopsFolder(), sectionName + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(shopFile);

        String itemsPath = pageName + ".items";
        if (!config.isConfigurationSection(itemsPath)) {
            return 0;
        }

        Set<String> itemKeys = config.getConfigurationSection(itemsPath).getKeys(false);
        int maxSlot = -1;

        for (String key : itemKeys) {
            try {
                int slot = Integer.parseInt(key);
                if (slot > maxSlot && slot <= 44) {
                    maxSlot = slot;
                }
            } catch (NumberFormatException e) {
                // Ignore invalid slot numbers
            }
        }

        int nextSlot = maxSlot + 1;
        if (nextSlot > 44) {
            return -1;
        }

        return nextSlot;
    }

    private void saveItemToFile(String sectionName, String pageName, int slot, String material, double buyPrice, double sellPrice) {
        File shopFile = new File(plugin.getConfigManager().getShopsFolder(), sectionName + ".yml");

        YamlConfiguration config;
        if (shopFile.exists()) {
            config = YamlConfiguration.loadConfiguration(shopFile);
        } else {
            config = new YamlConfiguration();
        }

        String itemPath = pageName + ".items." + slot;
        config.set(itemPath + ".material", material);
        config.set(itemPath + ".buy", buyPrice);
        config.set(itemPath + ".sell", sellPrice);

        try {
            config.save(shopFile);
        } catch (IOException e) {
            plugin.getLogger().warning("❌ Error saving item to file: " + sectionName);
            e.printStackTrace();
        }
    }
}
