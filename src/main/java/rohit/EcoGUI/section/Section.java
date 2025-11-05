package rohit.EcoGUI.section;

import org.bukkit.Material;

public class Section {

    private String name;
    private boolean enabled;
    private Material material;
    private String displayName;
    private int slot;

    public Section(String name, boolean enabled, Material material, String displayName, int slot) {
        this.name = name;
        this.enabled = enabled;
        this.material = material;
        this.displayName = displayName;
        this.slot = slot;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Material getMaterial() {
        return material;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getSlot() {
        return slot;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }
}
