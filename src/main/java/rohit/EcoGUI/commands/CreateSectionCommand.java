package rohit.EcoGUI.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import rohit.EcoGUI.Main;
import rohit.EcoGUI.section.Section;

import java.io.File;
import java.io.IOException;

public class CreateSectionCommand implements CommandExecutor {

    private Main plugin;

    public CreateSectionCommand(Main plugin) {
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
            player.sendMessage("§c❌ Usage: /csection <displayname> <slot> <icon>");
            return true;
        }

        String displayName = args[0];
        String slotStr = args[1];
        String iconStr = args[2];

        int slot;
        try {
            slot = Integer.parseInt(slotStr);
        } catch (NumberFormatException e) {
            player.sendMessage("§c❌ Slot must be a number!");
            return true;
        }

        if (slot < 0 || slot > 53) {
            player.sendMessage("§c❌ Slot must be between 0 and 53!");
            return true;
        }

        if (slot == 45 || slot == 53) {
            player.sendMessage("§c❌ Slot " + slot + " is reserved!");
            return true;
        }

        Material material;
        try {
            material = Material.valueOf(iconStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage("§c❌ Invalid material: " + iconStr);
            return true;
        }

        if (isSlotOccupied(slot)) {
            player.sendMessage("§c❌ Slot " + slot + " is already occupied!");
            return true;
        }

        String sectionName = displayName.toLowerCase().replace(" ", "_");
        createSectionFile(sectionName, displayName, slot, material);

        plugin.getSectionManager().loadSections();

        player.sendMessage("§a✅ Section created successfully!");
        player.sendMessage("§7Name: §e" + sectionName);
        player.sendMessage("§7Display: §e" + displayName);
        player.sendMessage("§7Slot: §e" + slot);
        player.sendMessage("§7Icon: §e" + iconStr);
        player.sendMessage("");
        player.sendMessage("§6💡 Run §e/rshop §6to reload and see changes in shop!");

        plugin.getLogger().info("✅ Section created: " + sectionName + " by " + player.getName());

        return true;
    }

    private boolean isSlotOccupied(int slot) {
        for (Section section : plugin.getSectionManager().getAllSections().values()) {
            if (section.getSlot() == slot) {
                return true;
            }
        }
        return false;
    }

    private void createSectionFile(String sectionName, String displayName, int slot, Material material) {
        File sectionsFolder = plugin.getConfigManager().getSectionsFolder();
        File sectionFile = new File(sectionsFolder, sectionName + ".yml");

        YamlConfiguration config = new YamlConfiguration();
        config.set("enable", true);
        config.set("item.material", material.name());
        config.set("item.displayname", displayName);
        config.set("slot", slot);

        try {
            config.save(sectionFile);
        } catch (IOException e) {
            plugin.getLogger().warning("❌ Error creating section file: " + sectionName);
            e.printStackTrace();
        }
    }
}
