package rohit.EcoGUI.shop;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import rohit.EcoGUI.Main;

public class BuyingSystem {

    private JavaPlugin plugin;
    private Economy economy;

    public BuyingSystem(JavaPlugin plugin, Economy economy) {
        this.plugin = plugin;
        this.economy = economy;
    }

    public boolean processBuy(Player player, ShopItem shopItem, int quantity) {
        if (quantity <= 0) {
            player.sendMessage("§c❌ Quantity must be greater than 0!");
            return false;
        }
        
        // Check max buy quantity from config
        if (plugin instanceof Main) {
            Main mainPlugin = (Main) plugin;
            int maxBuyQuantity = mainPlugin.getConfigManager().getMaxBuyQuantity();
            if (maxBuyQuantity > 0 && quantity > maxBuyQuantity) {
                player.sendMessage("§c❌ Maximum purchase quantity is " + maxBuyQuantity + "!");
                player.sendMessage("§7You tried to buy: §e" + quantity);
                return false;
            }
        }

        double totalPrice = shopItem.getBuyPrice() * quantity;

        if (!canAfford(player, totalPrice)) {
            double playerBalance = economy.getBalance(player);
            player.sendMessage("§c❌ Insufficient funds!");
            player.sendMessage("§7Need: §a$" + economy.format(totalPrice));
            player.sendMessage("§7Have: §c$" + economy.format(playerBalance));
            return false;
        }

        EconomyResponse withdrawResponse = economy.withdrawPlayer(player, totalPrice);

        if (!withdrawResponse.transactionSuccess()) {
            player.sendMessage("§c❌ Transaction failed: " + withdrawResponse.errorMessage);
            return false;
        }

        addItemsToInventory(player, shopItem.getMaterial(), quantity);

        sendSuccessMessage(player, shopItem, quantity, totalPrice);

        logTransaction(player, shopItem, quantity, totalPrice);

        return true;
    }

    public boolean canAfford(Player player, double price) {
        double playerBalance = economy.getBalance(player);
        return playerBalance >= price;
    }

    public double getPlayerBalance(Player player) {
        return economy.getBalance(player);
    }

    public String formatPrice(double price) {
        return economy.format(price);
    }

    public double calculateTotalPrice(ShopItem shopItem, int quantity) {
        return shopItem.getBuyPrice() * quantity;
    }

    private void sendSuccessMessage(Player player, ShopItem shopItem, int quantity, double totalPrice) {
        player.sendMessage("§a✅ Purchase successful!");
        player.sendMessage("§7Item: §e" + shopItem.getMaterial().name());
        player.sendMessage("§7Quantity: §e" + quantity);
        player.sendMessage("§7Total Cost: §c$" + economy.format(totalPrice));
        player.sendMessage("§7New Balance: §a$" + economy.format(economy.getBalance(player)));
    }

    private void logTransaction(Player player, ShopItem shopItem, int quantity, double totalPrice) {
        plugin.getLogger().info(
            player.getName() + " bought " + quantity + "x " + shopItem.getMaterial().name() + 
            " for $" + totalPrice
        );
    }

    public boolean depositMoney(Player player, double amount) {
        EconomyResponse response = economy.depositPlayer(player, amount);
        return response.transactionSuccess();
    }

    public boolean withdrawMoney(Player player, double amount) {
        EconomyResponse response = economy.withdrawPlayer(player, amount);
        return response.transactionSuccess();
    }

    private void addItemsToInventory(Player player, Material material, int quantity) {
        int maxStackSize = material.getMaxStackSize();
        int remaining = quantity;
        int totalDropped = 0;

        while (remaining > 0) {
            int stackSize = Math.min(remaining, maxStackSize);
            ItemStack itemStack = new ItemStack(material, stackSize);
            
            java.util.HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(itemStack);
            
            if (!leftover.isEmpty()) {
                for (ItemStack drop : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), drop);
                }
                totalDropped += leftover.values().stream().mapToInt(ItemStack::getAmount).sum();
            }
            
            remaining -= stackSize;
        }
        
        if (totalDropped > 0) {
            player.sendMessage("§e⚠️ Inventory full! Dropped " + totalDropped + " items.");
        }
    }
}
