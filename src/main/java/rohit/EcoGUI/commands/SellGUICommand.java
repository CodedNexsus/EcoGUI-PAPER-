package rohit.EcoGUI.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rohit.EcoGUI.Main;
import rohit.EcoGUI.listeners.SellGUIListener;

public class SellGUICommand implements CommandExecutor {

    private Main plugin;
    private SellGUIListener sellGUIListener;

    public SellGUICommand(Main plugin, SellGUIListener sellGUIListener) {
        this.plugin = plugin;
        this.sellGUIListener = sellGUIListener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;
        sellGUIListener.openSellGUI(player);
        return true;
    }
}
