package core.sg.barrelAddon.items;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import org.bukkit.Material;

public class BarrelItems {

    public static final SlimefunItemStack MINI_BARREL = new SlimefunItemStack(
            "MINI_BARREL",
            Material.BARREL,
            "&6Mini Barrel",
            "",
            "&7Capacity: &e1,024 &7items",
            "&7Stores only one item type",
            "",
            "&eRight-click to interact"
    );

    public static final SlimefunItemStack LARGE_BARREL = new SlimefunItemStack(
            "LARGE_BARREL",
            Material.BARREL,
            "&6Large Barrel",
            "",
            "&7Capacity: &e8,192 &7items",
            "&7Stores only one item type",
            "",
            "&eRight-click to interact"
    );

    public static final SlimefunItemStack MEGA_BARREL = new SlimefunItemStack(
            "MEGA_BARREL",
            Material.BARREL,
            "&6Mega Barrel",
            "",
            "&7Capacity: &e32,768 &7items",
            "&7Stores only one item type",
            "",
            "&eRight-click to interact"
    );
}
