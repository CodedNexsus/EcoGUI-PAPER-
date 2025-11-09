package rohit.EcoGUI;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import rohit.EcoGUI.commands.AddItemCommand;
import rohit.EcoGUI.commands.HoldItemCommand;
import rohit.EcoGUI.commands.CreateSectionCommand;
import rohit.EcoGUI.commands.ReloadCommand;
import rohit.EcoGUI.commands.SellCommand;
import rohit.EcoGUI.commands.SellAllCommand;
import rohit.EcoGUI.commands.SellGUICommand;
import rohit.EcoGUI.commands.ShopCommand;
import rohit.EcoGUI.config.ConfigManager;
import rohit.EcoGUI.listeners.InventoryListener;
import rohit.EcoGUI.listeners.SellGUIListener;
import rohit.EcoGUI.section.SectionManager;
import rohit.EcoGUI.helpers.MessageManager;

public class Main extends JavaPlugin {

    private Economy economy;
    private ConfigManager configManager;
    private SectionManager sectionManager;
    private ShopManager shopManager;
    private MessageManager messageManager;
    private SellGUIListener sellGUIListener;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        configManager.loadOrCreateFolders();

        messageManager = new MessageManager(configManager);

        sectionManager = new SectionManager(this, configManager.getSectionsFolder());
        sectionManager.loadSections();

        shopManager = new ShopManager(this, configManager.getShopsFolder());
        shopManager.loadShops();

        if (setupEconomy()) {
            getLogger().info("✅ EcoGUI Enabled - Vault linked successfully (Economy provider: " + economy.getName() + ")");
            registerListeners();
            registerCommands();
        } else {
            getLogger().warning("⚠️ EcoGUI Enabled - Vault not found or no Economy provider available");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("❌ EcoGUI Plugin Disabled!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new InventoryListener(this), this);
        sellGUIListener = new SellGUIListener(this);
        Bukkit.getPluginManager().registerEvents(sellGUIListener, this);
    }

    private void registerCommands() {
        getCommand("shop").setExecutor(new ShopCommand(economy, this));
        getCommand("rshop").setExecutor(new ReloadCommand(this));
        getCommand("csection").setExecutor(new CreateSectionCommand(this));
        getCommand("hitem").setExecutor(new HoldItemCommand(this));
        getCommand("aitem").setExecutor(new AddItemCommand(this));
        getCommand("sell").setExecutor(new SellCommand(this));
        getCommand("sellall").setExecutor(new SellAllCommand(this));
        getCommand("sellgui").setExecutor(new SellGUICommand(this, sellGUIListener));
    }

    public Economy getEconomy() {
        return economy;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public SectionManager getSectionManager() {
        return sectionManager;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }
}
