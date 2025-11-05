package rohit.EcoGUI.shop;

import java.util.HashMap;
import java.util.Map;

public class Shop {

    private String name;
    private Map<String, Map<Integer, ShopItem>> pages;

    public Shop(String name) {
        this.name = name;
        this.pages = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public Map<String, Map<Integer, ShopItem>> getPages() {
        return pages;
    }

    public Map<Integer, ShopItem> getPage(String pageName) {
        return pages.get(pageName);
    }

    public void addPage(String pageName, Map<Integer, ShopItem> items) {
        pages.put(pageName, items);
    }

    public ShopItem getItem(String pageName, int slot) {
        Map<Integer, ShopItem> pageItems = pages.get(pageName);
        if (pageItems != null) {
            return pageItems.get(slot);
        }
        return null;
    }

    public void addItem(String pageName, int slot, ShopItem item) {
        pages.computeIfAbsent(pageName, k -> new HashMap<>()).put(slot, item);
    }
}
