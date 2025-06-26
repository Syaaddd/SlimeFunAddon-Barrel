package core.sg.barrelAddon;

// ====== 1. Main Plugin Class ======
// src/main/java/com/yourname/barreladdon/BarrelAddon.java

import core.sg.barrelAddon.items.BarrelItems;
import core.sg.barrelAddon.setup.ItemSetup;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;

public class BarrelAddon extends JavaPlugin implements SlimefunAddon {

    private static BarrelAddon instance;
    private ItemGroup itemGroup;

    @Override
    public void onEnable() {
        instance = this;

        // Create Item Group
        setupItemGroup();

        // Register items
        ItemSetup.setup(this);

        getLogger().info("BarrelAddon has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("BarrelAddon has been disabled!");
    }

    private void setupItemGroup() {
        // Create the addon's item group
        SlimefunItemStack groupItem = new SlimefunItemStack(
                "BARREL_ADDON_GROUP",
                Material.BARREL,
                "&6Barrel Addon",
                "",
                "&7Storage solutions for Slimefun"
        );

        NamespacedKey groupKey = new NamespacedKey(this, "barrel_addon");
        this.itemGroup = new ItemGroup(groupKey, groupItem);
    }

    @Nonnull
    @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    @Override
    public String getBugTrackerURL() {
        return "https://github.com/YourUsername/BarrelAddon/issues";
    }

    public static BarrelAddon getInstance() {
        return instance;
    }

    public ItemGroup getItemGroup() {
        return itemGroup;
    }
}




