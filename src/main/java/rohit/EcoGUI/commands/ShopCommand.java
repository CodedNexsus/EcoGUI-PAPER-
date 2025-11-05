package rohit.EcoGUI.commands;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import rohit.EcoGUI.Main;
import rohit.EcoGUI.inventory.ShopInventoryHolder;
import rohit.EcoGUI.section.Section;

public class ShopCommand implements CommandExecutor {

    private Economy economy;
    private Main plugin;

    public ShopCommand(Economy economy, Main plugin) {
        this.economy = economy;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;
        openShopGUI(player);
        return true;
    }

    private void openShopGUI(Player player) {
        Inventory shopInventory = Bukkit.createInventory(new ShopInventoryHolder(null), 54, "§6Shop");

        addSections(shopInventory);

        ItemStack playerHead = createPlayerHead(player);
        shopInventory.setItem(45, playerHead);

        ItemStack closeButton = createCloseButton();
        shopInventory.setItem(53, closeButton);

        player.openInventory(shopInventory);
    }

    private void addSections(Inventory inventory) {
        for (Section section : plugin.getSectionManager().getEnabledSections()) {
            int slot = section.getSlot();
            
            if (slot >= 0 && slot <= 53 && slot != 45 && slot != 53) {
                ItemStack sectionItem = createSectionItem(section);
                inventory.setItem(slot, sectionItem);
            }
        }
    }

    private ItemStack createSectionItem(Section section) {
        ItemStack item = new ItemStack(section.getMaterial());
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§e" + section.getDisplayName());
            item.setItemMeta(meta);
        }
        
        return item;
    }

    private ItemStack createPlayerHead(Player player) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(player);
            
            double balance = economy.getBalance(player);
            String formattedBalance = economy.format(balance);
            
            skullMeta.setDisplayName("§e" + player.getName());
            skullMeta.setLore(java.util.Arrays.asList(
                "§7Balance: §a" + formattedBalance
            ));
            
            head.setItemMeta(skullMeta);
        }
        
        return head;
    }

    private ItemStack createCloseButton() {
        ItemStack closeButton = new ItemStack(Material.BARRIER);
        ItemMeta meta = closeButton.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§c✕ Close");
            closeButton.setItemMeta(meta);
        }
        
        return closeButton;
    }
}
