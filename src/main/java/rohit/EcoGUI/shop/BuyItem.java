package rohit.EcoGUI.shop;

import org.bukkit.Material;

public class BuyItem {

    private Material material;
    private double buyPrice;
    private int quantity;

    public BuyItem(Material material, double buyPrice, int quantity) {
        this.material = material;
        this.buyPrice = buyPrice;
        this.quantity = quantity;
    }

    public Material getMaterial() {
        return material;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        return buyPrice * quantity;
    }
}
