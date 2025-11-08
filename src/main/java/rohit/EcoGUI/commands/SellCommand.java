package rohit.EcoGUI.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import rohit.EcoGUI.Main;
import rohit.EcoGUI.shop.SellingSystem;

public class SellCommand implements CommandExecutor {

    private Main plugin;
    private SellingSystem sellingSystem;

    public SellCommand(Main plugin) {
        this.plugin = plugin;
        this.sellingSystem = new SellingSystem(plugin, plugin.getEconomy());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage("§c❌ Usage: /sell <item name> <amount> or /sell hand [amount]");
            return true;
        }

        String firstArg = args[0].toLowerCase();

        if (firstArg.equals("hand")) {
            return handleHandSell(player, args);
        }

        if (args.length < 2) {
            player.sendMessage("§c❌ Usage: /sell <item name> <amount> or /sell hand [amount]");
            return true;
        }

        return handleItemSell(player, args);
    }

    private boolean handleHandSell(Player player, String[] args) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand == null || itemInHand.getType() == Material.AIR) {
            player.sendMessage("§c❌ You must be holding an item!");
            return true;
        }

        Material material = itemInHand.getType();
        int amount;

        if (args.length < 2) {
            amount = countItemInInventory(player, material);
            if (amount == 0) {
                player.sendMessage("§c❌ You don't have any " + material.name() + " in your inventory!");
                return true;
            }
        } else {
            String amountStr = args[1];
            try {
                amount = Integer.parseInt(amountStr);
            } catch (NumberFormatException e) {
                player.sendMessage("§c❌ Amount must be a number!");
                return true;
            }

            if (amount <= 0) {
                player.sendMessage("§c❌ Amount must be greater than 0!");
                return true;
            }
        }

        int itemCount = countItemInInventory(player, material);
        if (itemCount < amount) {
            player.sendMessage("§c❌ Insufficient items!");
            player.sendMessage("§7Need: §e" + amount);
            player.sendMessage("§7Have: §c" + itemCount);
            return true;
        }

        sellingSystem.processSell(player, material, amount);

        return true;
    }

    private boolean handleItemSell(Player player, String[] args) {
        String itemName = args[0];
        String amountStr = args[1];

        Material material;
        try {
            material = Material.valueOf(itemName.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage("§c❌ Invalid material: " + itemName);
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            player.sendMessage("§c❌ Amount must be a number!");
            return true;
        }

        if (amount <= 0) {
            player.sendMessage("§c❌ Amount must be greater than 0!");
            return true;
        }

        int itemCount = countItemInInventory(player, material);
        if (itemCount < amount) {
            player.sendMessage("§c❌ Insufficient items!");
            player.sendMessage("§7Need: §e" + amount);
            player.sendMessage("§7Have: §c" + itemCount);
            return true;
        }

        sellingSystem.processSell(player, material, amount);

        return true;
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
}
