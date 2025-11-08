package rohit.EcoGUI.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.ChatColor;
import rohit.EcoGUI.Main;
import rohit.EcoGUI.inventory.ShopInventoryHolder;
import rohit.EcoGUI.inventory.SellInventoryHolder;
import rohit.EcoGUI.section.Section;
import rohit.EcoGUI.shop.Shop;
import rohit.EcoGUI.shop.ShopItem;
import rohit.EcoGUI.shop.BuyingSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InventoryListener implements Listener {

    private final Main plugin;
    private final NamespacedKey pageKey;
    private final NamespacedKey buyItemKey;
    private final NamespacedKey buyQtyKey;
    private final NamespacedKey buySectionKey;
    private final NamespacedKey buyPageKey;
    private final BuyingSystem buyingSystem;

    public InventoryListener(Main plugin) {
        this.plugin = plugin;
        this.pageKey = new NamespacedKey(plugin, "shop_page");
        this.buyItemKey = new NamespacedKey(plugin, "buy_material");
        this.buyQtyKey = new NamespacedKey(plugin, "buy_quantity");
        this.buySectionKey = new NamespacedKey(plugin, "buy_section");
        this.buyPageKey = new NamespacedKey(plugin, "buy_page");
        this.buyingSystem = new BuyingSystem(plugin, plugin.getEconomy());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Inventory clickedInv = event.getClickedInventory();
        if (clickedInv == null) return;
        
        if (clickedInv.getHolder() instanceof SellInventoryHolder) {
            return;
        }
        
        if (!(clickedInv.getHolder() instanceof ShopInventoryHolder)) {
            return;
        }
        
        event.setCancelled(true);
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        int slot = event.getSlot();
        
        if (title.equals("Buy Item")) {
            handleBuyGuiClick(player, clickedInv, slot);
            return;
        }
        
        if (title.equals("Shop")) {
            if (slot == 53) {
                player.closeInventory();
            } else {
                handleSectionClick(player, slot);
            }
            return;
        }
        
        if (slot == 53) {
            handleBackToSections(player);
        } else if (slot == 52) {
            player.closeInventory();
        } else if (slot == 46) {
            handlePreviousPage(player);
        } else if (slot == 51) {
            handleNextPage(player);
        } else if (slot <= 44) {
            handleItemClick(player, slot);
        }
    }

    private void handleSectionClick(Player player, int slot) {
        Section section = getSectionBySlot(slot);
        if (section != null) {
            openShopItems(player, section.getName());
        }
    }

    private Section getSectionBySlot(int slot) {
        for (Section section : plugin.getSectionManager().getAllSections().values()) {
            if (section.getSlot() == slot && section.isEnabled()) {
                return section;
            }
        }
        return null;
    }

    private void openShopItems(Player player, String sectionName) {
        openShopItemsPage(player, sectionName, "page1");
    }

    private void openShopItemsPage(Player player, String sectionName, String pageName) {
        Shop shop = plugin.getShopManager().getShop(sectionName);
        if (shop == null) {
            player.sendMessage("§c❌ Shop not found for section: " + sectionName);
            return;
        }

        Map<Integer, ShopItem> pageItems = shop.getPage(pageName);
        if (pageItems == null || pageItems.isEmpty()) {
            player.sendMessage("§c❌ No items found in shop: " + sectionName);
            return;
        }

        Inventory shopItemsInventory = Bukkit.createInventory(new ShopInventoryHolder(null), 54, sectionName + " - " + pageName);

        for (Map.Entry<Integer, ShopItem> entry : pageItems.entrySet()) {
            int slot = entry.getKey();
            ShopItem shopItem = entry.getValue();

            if (slot >= 0 && slot <= 44) {
                ItemStack item = createShopItemStack(shopItem);
                shopItemsInventory.setItem(slot, item);
            }
        }

        fillEmptySlots(shopItemsInventory);

        ItemStack playerHead = createPlayerHead(player);
        shopItemsInventory.setItem(45, playerHead);

        ItemStack redPanel = createRedPanel();
        shopItemsInventory.setItem(46, redPanel);

        ItemStack bluePanel = createBluePanel();
        shopItemsInventory.setItem(51, bluePanel);

        ItemStack backButton = createBackButton();
        shopItemsInventory.setItem(53, backButton);

        ItemStack closeButton = createCloseButton();
        shopItemsInventory.setItem(52, closeButton);

        player.openInventory(shopItemsInventory);

        ItemMeta meta = new ItemStack(Material.STONE).getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(pageKey, PersistentDataType.STRING, pageName);
        }
    }

    private void fillEmptySlots(Inventory inventory) {
        ItemStack whitePanel = createWhitePanel();
        for (int i = 47; i <= 50; i++) {
            inventory.setItem(i, whitePanel);
        }
    }

    private void handleBackToSections(Player player) {
        Inventory shopInventory = Bukkit.createInventory(new ShopInventoryHolder(null), 54, "Shop");
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
            String displayName = section.getDisplayName();
            if (displayName.startsWith("'") && displayName.endsWith("'")) {
                displayName = displayName.substring(1, displayName.length() - 1);
            }
            if (!displayName.contains("§") && !displayName.contains("&")) {
                displayName = "§f" + displayName;
            }
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
            item.setItemMeta(meta);
        }
        return item;
    }

    private void handlePreviousPage(Player player) {
        String title = player.getOpenInventory().getTitle();
        String sectionName = extractSectionNameFromTitle(title);
        String currentPageStr = extractPageFromTitle(title);

        int currentPageNum = Integer.parseInt(currentPageStr.replace("page", ""));
        if (currentPageNum <= 1) {
            player.sendMessage("§c❌ Already on first page!");
            return;
        }

        String previousPage = "page" + (currentPageNum - 1);
        openShopItemsPage(player, sectionName, previousPage);
    }

    private void handleNextPage(Player player) {
        String title = player.getOpenInventory().getTitle();
        String sectionName = extractSectionNameFromTitle(title);
        String currentPageStr = extractPageFromTitle(title);

        int currentPageNum = Integer.parseInt(currentPageStr.replace("page", ""));
        String nextPage = "page" + (currentPageNum + 1);

        Shop shop = plugin.getShopManager().getShop(sectionName);
        if (shop == null || shop.getPage(nextPage) == null) {
            player.sendMessage("§c❌ No more pages available!");
            return;
        }

        openShopItemsPage(player, sectionName, nextPage);
    }

    private String extractSectionNameFromTitle(String title) {
        if (title.contains(" - ")) {
            String[] parts = title.split(" - ");
            return parts[0];
        }
        return title;
    }

    private String extractPageFromTitle(String title) {
        if (title.contains("page")) {
            String[] parts = title.split("page");
            if (parts.length > 1) {
                try {
                    int pageNum = Integer.parseInt(parts[1].trim());
                    return "page" + pageNum;
                } catch (NumberFormatException e) {
                    return "page1";
                }
            }
        }
        return "page1";
    }

    private ItemStack createShopItemStack(ShopItem shopItem) {
        ItemStack item = new ItemStack(shopItem.getMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(shopItem.getMaterial().name());
            List<String> lore = new ArrayList<>();
            lore.add("§7Buy: §a$" + shopItem.getBuyPrice());
            if (shopItem.getSellPrice() == -1.0) {
                lore.add("§7Sell: §c❌ Cannot be sold");
            } else {
                lore.add("§7Sell: §c$" + shopItem.getSellPrice());
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createPlayerHead(Player player) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        org.bukkit.inventory.meta.SkullMeta skullMeta = (org.bukkit.inventory.meta.SkullMeta) head.getItemMeta();
        if (skullMeta != null) {
            skullMeta.setOwningPlayer(player);
            double balance = plugin.getEconomy().getBalance(player);
            String formattedBalance = plugin.getEconomy().format(balance);
            skullMeta.setDisplayName(player.getName());
            skullMeta.setLore(java.util.Arrays.asList(
                "§7Balance: §a" + formattedBalance
            ));
            head.setItemMeta(skullMeta);
        }
        return head;
    }

    private ItemStack createBackButton() {
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta meta = backButton.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("← Back to Sections");
            backButton.setItemMeta(meta);
        }
        return backButton;
    }

    private ItemStack createCloseButton() {
        ItemStack closeButton = new ItemStack(Material.BARRIER);
        ItemMeta meta = closeButton.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("✕ Close Shop");
            closeButton.setItemMeta(meta);
        }
        return closeButton;
    }

    private ItemStack createRedPanel() {
        ItemStack redPanel = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta meta = redPanel.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("← Previous Page");
            redPanel.setItemMeta(meta);
        }
        return redPanel;
    }

    private ItemStack createBluePanel() {
        ItemStack bluePanel = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
        ItemMeta meta = bluePanel.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("→ Next Page");
            bluePanel.setItemMeta(meta);
        }
        return bluePanel;
    }

    private ItemStack createWhitePanel() {
        ItemStack whitePanel = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
        ItemMeta meta = whitePanel.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            whitePanel.setItemMeta(meta);
        }
        return whitePanel;
    }

    private void handleItemClick(Player player, int slot) {
        String title = player.getOpenInventory().getTitle();
        String sectionName = title.replace("Shop", "");
        String currentPageStr = extractPageFromTitle(title);
        Shop shop = plugin.getShopManager().getShop(sectionName);
        if (shop == null) return;
        Map<Integer, ShopItem> pageItems = shop.getPage(currentPageStr);
        if (pageItems == null) return;
        ShopItem shopItem = pageItems.get(slot);
        if (shopItem == null) return;
        openBuyUI(player, shopItem, sectionName, currentPageStr);
    }

    private void openBuyUI(Player player, ShopItem shopItem, String sectionName, String pageName) {
        Inventory buyInventory = Bukkit.createInventory(new ShopInventoryHolder(null), 36, "Buy Item");

        ItemStack itemDisplay = new ItemStack(shopItem.getMaterial());
        ItemMeta meta = itemDisplay.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(formatMaterialName(shopItem.getMaterial()));
            itemDisplay.setItemMeta(meta);
        }
        buyInventory.setItem(13, itemDisplay);

        ItemStack priceDisplay = new ItemStack(Material.PAPER);
        ItemMeta priceMeta = priceDisplay.getItemMeta();
        if (priceMeta != null) {
            priceMeta.setDisplayName("§6Price: §a$" + shopItem.getBuyPrice());
            priceMeta.getPersistentDataContainer().set(buyQtyKey, PersistentDataType.INTEGER, 1);
            priceMeta.getPersistentDataContainer().set(buyItemKey, PersistentDataType.STRING, shopItem.getMaterial().name());
            priceMeta.getPersistentDataContainer().set(buySectionKey, PersistentDataType.STRING, sectionName);
            priceMeta.getPersistentDataContainer().set(buyPageKey, PersistentDataType.STRING, pageName);
            priceMeta.setLore(java.util.Arrays.asList(
                "§7Quantity: §e1",
                "§7Total: §a$" + shopItem.getBuyPrice()
            ));
            priceDisplay.setItemMeta(priceMeta);
        }
        buyInventory.setItem(4, priceDisplay);

        ItemStack sub32 = createQuantityButton("§c-32", Material.RED_STAINED_GLASS_PANE);
        ItemStack sub16 = createQuantityButton("§c-16", Material.RED_STAINED_GLASS_PANE);
        ItemStack sub1  = createQuantityButton("§c-1",  Material.RED_STAINED_GLASS_PANE);
        ItemStack add1  = createQuantityButton("§a+1",  Material.LIME_STAINED_GLASS_PANE);
        ItemStack add16 = createQuantityButton("§a+16", Material.LIME_STAINED_GLASS_PANE);
        ItemStack add32 = createQuantityButton("§a+32", Material.LIME_STAINED_GLASS_PANE);
        ItemStack add64 = createQuantityButton("§a+64", Material.LIME_STAINED_GLASS_PANE);
        ItemStack sub64 = createQuantityButton("§c-64", Material.RED_STAINED_GLASS_PANE);

        ItemStack glass = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glass.setItemMeta(glassMeta);
        }
        ItemStack separator = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta sepMeta = separator.getItemMeta();
        if (sepMeta != null) {
            sepMeta.setDisplayName(" ");
            separator.setItemMeta(sepMeta);
        }

        buyInventory.setItem(18, add1);
        buyInventory.setItem(19, add16);
        buyInventory.setItem(20, add32);
        buyInventory.setItem(21, add64);
        buyInventory.setItem(22, separator);
        buyInventory.setItem(23, sub1);
        buyInventory.setItem(24, sub16);
        buyInventory.setItem(25, sub32);
        buyInventory.setItem(26, sub64);

        ItemStack playerHead = createPlayerHead(player);
        buyInventory.setItem(27, playerHead);



        ItemStack closeButton = createCloseButton();
        buyInventory.setItem(34, closeButton);

        ItemStack backButton = createBackButton();
        buyInventory.setItem(35, backButton);

        player.openInventory(buyInventory);
    }

    private void handleBuyGuiClick(Player player, Inventory inv, int slot) {
        if (slot == 34) {
            player.closeInventory();
            return;
        }
        if (slot == 35) {
            ItemStack paper = inv.getItem(4);
            if (paper != null && paper.getType() == Material.PAPER) {
                ItemMeta pMeta = paper.getItemMeta();
                if (pMeta != null) {
                    String sectionName = pMeta.getPersistentDataContainer().get(buySectionKey, PersistentDataType.STRING);
                    String pageName = pMeta.getPersistentDataContainer().get(buyPageKey, PersistentDataType.STRING);
                    if (sectionName != null && pageName != null) {
                        openShopItemsPage(player, sectionName, pageName);
                        return;
                    }
                }
            }
            handleBackToSections(player);
            return;
        }

        ItemStack paper = inv.getItem(4);
        ItemStack item = inv.getItem(13);
        if (paper == null || item == null || paper.getType() != Material.PAPER) return;

        ItemMeta pMeta = paper.getItemMeta();
        if (pMeta == null) return;

        Integer qty = pMeta.getPersistentDataContainer().get(buyQtyKey, PersistentDataType.INTEGER);
        String matName = pMeta.getPersistentDataContainer().get(buyItemKey, PersistentDataType.STRING);
        if (qty == null) qty = 1;
        if (matName == null) matName = item.getType().name();

        double unitPrice = extractUnitPriceFromPaper(pMeta.getDisplayName());
        if (unitPrice < 0) unitPrice = 0;

        int delta = 0;
        if (slot == 18) delta = +1;
        else if (slot == 19) delta = +16;
        else if (slot == 20) delta = +32;
        else if (slot == 21) delta = +64;
        else if (slot == 23) delta = -1;
        else if (slot == 24) delta = -16;
        else if (slot == 25) delta = -32;
        else if (slot == 26) delta = -64;

        if (delta != 0) {
            qty = Math.max(1, Math.min(9999, qty + delta));
            updatePaper(paper, pMeta, qty, unitPrice);
            inv.setItem(4, paper);
            return;
        }

        if (slot == 4) {
            Material material;
            try {
                material = Material.valueOf(matName);
            } catch (IllegalArgumentException e) {
                player.sendMessage(ChatColor.RED + " Invalid item to buy.");
                return;
            }
            ShopItem shopItem = new ShopItem(material, unitPrice, -1.0, 0);
            buyingSystem.processBuy(player, shopItem, qty);
        }
    }

    private double extractUnitPriceFromPaper(String displayName) {
        if (displayName == null) return -1;
        String plain = ChatColor.stripColor(displayName);
        int idx = plain.indexOf('$');
        if (idx == -1) return -1;
        try {
            return Double.parseDouble(plain.substring(idx + 1).trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void updatePaper(ItemStack paper, ItemMeta pMeta, int qty, double unitPrice) {
        pMeta.getPersistentDataContainer().set(buyQtyKey, PersistentDataType.INTEGER, qty);
        pMeta.setLore(java.util.Arrays.asList(
            "§7Quantity: §e" + qty,
            "§7Total: §a$" + (unitPrice * qty)
        ));
        paper.setItemMeta(pMeta);
    }

    private ItemStack createQuantityButton(String displayName, Material material) {
        ItemStack button = new ItemStack(material);
        ItemMeta meta = button.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            button.setItemMeta(meta);
        }
        return button;
    }

    private String formatMaterialName(Material material) {
        String name = material.name().toLowerCase();
        String[] parts = name.split("_");
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                formatted.append(" ");
            }
            formatted.append(parts[i].substring(0, 1).toUpperCase()).append(parts[i].substring(1));
        }
        return formatted.toString();
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (top == null) return;
        
        if (top.getHolder() instanceof SellInventoryHolder) {
            return;
        }
        
        if (top.getHolder() instanceof ShopInventoryHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Inventory top = event.getPlayer().getOpenInventory().getTopInventory();
        if (top == null) return;
        
        if (top.getHolder() instanceof SellInventoryHolder) {
            return;
        }
        
        if (top.getHolder() instanceof ShopInventoryHolder) {
            event.setCancelled(true);
        }
    }
}
