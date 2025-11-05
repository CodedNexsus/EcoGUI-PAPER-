package rohit.EcoGUI.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rohit.EcoGUI.Main;

public class ReloadCommand implements CommandExecutor {

    private Main plugin;

    public ReloadCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        plugin.getSectionManager().loadSections();
        plugin.getShopManager().loadShops();

        player.sendMessage("§a✅ Configuration reloaded successfully!");
        player.sendMessage("§7Sections: §e" + plugin.getSectionManager().getTotalSections());
        player.sendMessage("§7Shops: §e" + plugin.getShopManager().getTotalShops());
        player.sendMessage("§7Selling Prices: §a✅ Reloaded from shop configuration");

        plugin.getLogger().info("✅ Configuration reloaded by " + player.getName());
        plugin.getLogger().info("✅ Selling prices reloaded from shop configuration");

        return true;
    }
}
