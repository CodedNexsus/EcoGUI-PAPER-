package rohit.EcoGUI.shop;

import org.bukkit.Material;

public class ShopItem {

    private Material material;
    private double buyPrice;
    private double sellPrice;
    private int slot;

    public ShopItem(Material material, double buyPrice, double sellPrice, int slot) {
        this.material = material;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.slot = slot;
    }

    public Material getMaterial() {
        return material;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public int getSlot() {
        return slot;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public void setBuyPrice(double buyPrice) {
        this.buyPrice = buyPrice;
    }

    public void setSellPrice(double sellPrice) {
        this.sellPrice = sellPrice;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }
}
