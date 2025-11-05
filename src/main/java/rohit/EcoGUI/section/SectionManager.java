package rohit.EcoGUI.section;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SectionManager {

    private JavaPlugin plugin;
    private File sectionsFolder;
    private Map<String, Section> sections;

    public SectionManager(JavaPlugin plugin, File sectionsFolder) {
        this.plugin = plugin;
        this.sectionsFolder = sectionsFolder;
        this.sections = new HashMap<>();
    }

    public void loadSections() {
        if (!sectionsFolder.exists()) {
            plugin.getLogger().warning("⚠️ Sections folder does not exist!");
            return;
        }

        File[] files = sectionsFolder.listFiles((dir, name) -> name.endsWith(".yml"));

        if (files == null || files.length == 0) {
            plugin.getLogger().info("📂 No section files found in sections folder");
            return;
        }

        for (File file : files) {
            loadSection(file);
        }

        plugin.getLogger().info("✅ Loaded " + sections.size() + " sections");
    }

    private void loadSection(File file) {
        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            String sectionName = file.getName().replace(".yml", "");

            boolean enabled = config.getBoolean("enable", false);
            String materialName = config.getString("item.material", "STONE");
            String displayName = config.getString("item.displayname", sectionName);
            int slot = config.getInt("slot", 0);

            Material material;
            try {
                material = Material.valueOf(materialName.toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("⚠️ Invalid material '" + materialName + "' in section: " + sectionName);
                material = Material.STONE;
            }

            Section section = new Section(sectionName, enabled, material, displayName, slot);
            sections.put(sectionName, section);

            if (enabled) {
                plugin.getLogger().info("✅ Loaded section: " + sectionName);
            } else {
                plugin.getLogger().info("⏸️ Section disabled: " + sectionName);
            }

        } catch (Exception e) {
            plugin.getLogger().warning("❌ Error loading section file: " + file.getName());
            e.printStackTrace();
        }
    }

    public Section getSection(String name) {
        return sections.get(name);
    }

    public List<Section> getEnabledSections() {
        List<Section> enabledSections = new ArrayList<>();
        for (Section section : sections.values()) {
            if (section.isEnabled()) {
                enabledSections.add(section);
            }
        }
        return enabledSections;
    }

    public Map<String, Section> getAllSections() {
        return sections;
    }

    public int getTotalSections() {
        return sections.size();
    }

    public int getEnabledSectionsCount() {
        return (int) sections.values().stream().filter(Section::isEnabled).count();
    }
}
