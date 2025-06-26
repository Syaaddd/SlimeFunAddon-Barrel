package core.sg.barrelAddon.setup;

import core.sg.barrelAddon.items.BarrelItems;
import core.sg.barrelAddon.setup.ItemSetup;
import core.sg.barrelAddon.items.CustomBarrel;
import core.sg.barrelAddon.BarrelAddon;
import core.sg.barrelAddon.items.BarrelItems;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemSetup {

    public static void setup(core.sg.barrelAddon.BarrelAddon plugin) {

        // Mini Barrel Recipe
        ItemStack[] miniBarrelRecipe = {
                new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS),
                new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.CHEST), new ItemStack(Material.OAK_PLANKS),
                new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS)
        };

        // Large Barrel Recipe
        ItemStack[] largeBarrelRecipe = {
                new ItemStack(Material.IRON_INGOT), core.sg.barrelAddon.items.BarrelItems.MINI_BARREL, new ItemStack(Material.IRON_INGOT),
                core.sg.barrelAddon.items.BarrelItems.MINI_BARREL, new ItemStack(Material.ENDER_CHEST), core.sg.barrelAddon.items.BarrelItems.MINI_BARREL,
                new ItemStack(Material.IRON_INGOT), core.sg.barrelAddon.items.BarrelItems.MINI_BARREL, new ItemStack(Material.IRON_INGOT)
        };

        // Mega Barrel Recipe
        ItemStack[] megaBarrelRecipe = {
                new ItemStack(Material.DIAMOND), core.sg.barrelAddon.items.BarrelItems.LARGE_BARREL, new ItemStack(Material.DIAMOND),
                core.sg.barrelAddon.items.BarrelItems.LARGE_BARREL, new ItemStack(Material.SHULKER_BOX), core.sg.barrelAddon.items.BarrelItems.LARGE_BARREL,
                new ItemStack(Material.DIAMOND), core.sg.barrelAddon.items.BarrelItems.LARGE_BARREL, new ItemStack(Material.DIAMOND)
        };

        // Register items
        new core.sg.barrelAddon.items.CustomBarrel(
                plugin.getItemGroup(),
                core.sg.barrelAddon.items.BarrelItems.MINI_BARREL,
                RecipeType.ENHANCED_CRAFTING_TABLE,
                miniBarrelRecipe,
                1024
        ).register(plugin);

        new core.sg.barrelAddon.items.CustomBarrel(
                plugin.getItemGroup(),
                core.sg.barrelAddon.items.BarrelItems.LARGE_BARREL,
                RecipeType.ENHANCED_CRAFTING_TABLE,
                largeBarrelRecipe,
                8192
        ).register(plugin);

        new core.sg.barrelAddon.items.CustomBarrel(
                plugin.getItemGroup(),
                core.sg.barrelAddon.items.BarrelItems.MEGA_BARREL,
                RecipeType.ENHANCED_CRAFTING_TABLE,
                megaBarrelRecipe,
                32768
        ).register(plugin);
    }
}