package core.sg.barrelAddon;

// ====== 1. Main Plugin Class ======
// src/main/java/com/yourname/barreladdon/BarrelAddon.java

package com.yourname.barreladdon;

import com.yourname.barreladdon.items.BarrelItems;
import com.yourname.barreladdon.setup.ItemSetup;
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
        this.itemGroup = ItemGroup.create(groupKey, groupItem);
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

// ====== 2. Item Definitions ======
// src/main/java/com/yourname/barreladdon/items/BarrelItems.java

package com.yourname.barreladdon.items;

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

// ====== 3. Item Setup ======
// src/main/java/com/yourname/barreladdon/setup/ItemSetup.java

package com.yourname.barreladdon.setup;

import com.yourname.barreladdon.BarrelAddon;
import com.yourname.barreladdon.items.BarrelItems;
import com.yourname.barreladdon.items.CustomBarrel;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemSetup {

    public static void setup(BarrelAddon plugin) {

        // Mini Barrel Recipe
        ItemStack[] miniBarrelRecipe = {
                new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS),
                new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.CHEST), new ItemStack(Material.OAK_PLANKS),
                new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS), new ItemStack(Material.OAK_PLANKS)
        };

        // Large Barrel Recipe
        ItemStack[] largeBarrelRecipe = {
                new ItemStack(Material.IRON_INGOT), BarrelItems.MINI_BARREL, new ItemStack(Material.IRON_INGOT),
                BarrelItems.MINI_BARREL, new ItemStack(Material.ENDER_CHEST), BarrelItems.MINI_BARREL,
                new ItemStack(Material.IRON_INGOT), BarrelItems.MINI_BARREL, new ItemStack(Material.IRON_INGOT)
        };

        // Mega Barrel Recipe
        ItemStack[] megaBarrelRecipe = {
                new ItemStack(Material.DIAMOND), BarrelItems.LARGE_BARREL, new ItemStack(Material.DIAMOND),
                BarrelItems.LARGE_BARREL, new ItemStack(Material.SHULKER_BOX), BarrelItems.LARGE_BARREL,
                new ItemStack(Material.DIAMOND), BarrelItems.LARGE_BARREL, new ItemStack(Material.DIAMOND)
        };

        // Register items
        new CustomBarrel(
                plugin.getItemGroup(),
                BarrelItems.MINI_BARREL,
                RecipeType.ENHANCED_CRAFTING_TABLE,
                miniBarrelRecipe,
                1024
        ).register(plugin);

        new CustomBarrel(
                plugin.getItemGroup(),
                BarrelItems.LARGE_BARREL,
                RecipeType.ENHANCED_CRAFTING_TABLE,
                largeBarrelRecipe,
                8192
        ).register(plugin);

        new CustomBarrel(
                plugin.getItemGroup(),
                BarrelItems.MEGA_BARREL,
                RecipeType.ENHANCED_CRAFTING_TABLE,
                megaBarrelRecipe,
                32768
        ).register(plugin);
    }
}

// ====== 4. Custom Barrel Implementation ======
// src/main/java/com/yourname/barreladdon/items/CustomBarrel.java

package com.yourname.barreladdon.items;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.data.persistent.PersistentDataAPI;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class CustomBarrel extends SlimefunItem implements NotPlaceable {

    private final int capacity;
    private final NamespacedKey itemKey;
    private final NamespacedKey amountKey;

    public CustomBarrel(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType,
                        ItemStack[] recipe, int capacity) {
        super(itemGroup, item, recipeType, recipe);

        this.capacity = capacity;
        this.itemKey = new NamespacedKey(Slimefun.instance(), "barrel_item");
        this.amountKey = new NamespacedKey(Slimefun.instance(), "barrel_amount");

        addItemHandler(onRightClick());
    }

    @Nonnull
    private ItemUseHandler onRightClick() {
        return e -> {
            e.cancel();

            Player player = e.getPlayer();
            ItemStack barrel = e.getItem();
            ItemStack playerItem = getOtherHandItem(player, barrel);

            if (playerItem == null || playerItem.getType() == Material.AIR) {
                // Try to take items from barrel
                takeFromBarrel(player, barrel);
            } else {
                // Try to add items to barrel
                addToBarrel(player, barrel, playerItem);
            }
        };
    }

    @Nullable
    private ItemStack getOtherHandItem(@Nonnull Player player, @Nonnull ItemStack currentItem) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        if (SlimefunUtils.isItemSimilar(mainHand, currentItem, true, false)) {
            return offHand;
        } else {
            return mainHand;
        }
    }

    private void addToBarrel(@Nonnull Player player, @Nonnull ItemStack barrel, @Nonnull ItemStack playerItem) {
        if (!isValidItem(playerItem)) {
            player.sendMessage(ChatColor.RED + "This item cannot be stored in the barrel!");
            return;
        }

        ItemMeta barrelMeta = barrel.getItemMeta();
        if (barrelMeta == null) return;

        // Get stored item and amount
        String storedItemData = PersistentDataAPI.getString(barrelMeta, itemKey);
        int storedAmount = PersistentDataAPI.getInt(barrelMeta, amountKey, 0);

        // Check if barrel is empty or contains same item
        if (storedAmount == 0 || storedItemData == null || storedItemData.isEmpty()) {
            // Empty barrel - store new item
            storeNewItem(player, barrel, playerItem, barrelMeta);
        } else {
            // Check if same item type
            ItemStack storedItem = deserializeItem(storedItemData);
            if (storedItem != null && isSameItem(storedItem, playerItem)) {
                addSameItem(player, barrel, playerItem, barrelMeta, storedAmount);
            } else {
                player.sendMessage(ChatColor.RED + "Barrel already contains a different item!");
            }
        }
    }

    private void storeNewItem(@Nonnull Player player, @Nonnull ItemStack barrel,
                              @Nonnull ItemStack playerItem, @Nonnull ItemMeta barrelMeta) {
        int amountToStore = Math.min(playerItem.getAmount(), capacity);

        // Store item data
        String itemData = serializeItem(playerItem);
        PersistentDataAPI.setString(barrelMeta, itemKey, itemData);
        PersistentDataAPI.setInt(barrelMeta, amountKey, amountToStore);

        // Update barrel appearance and lore
        updateBarrelMeta(barrelMeta, playerItem, amountToStore);
        barrel.setItemMeta(barrelMeta);

        // Remove items from player
        if (playerItem.getAmount() <= amountToStore) {
            playerItem.setAmount(0);
        } else {
            playerItem.setAmount(playerItem.getAmount() - amountToStore);
        }

        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.0f);
        player.sendMessage(ChatColor.GREEN + "Stored " + amountToStore + "x " +
                getItemDisplayName(playerItem) + " in barrel!");
    }

    private void addSameItem(@Nonnull Player player, @Nonnull ItemStack barrel,
                             @Nonnull ItemStack playerItem, @Nonnull ItemMeta barrelMeta, int currentAmount) {
        int availableSpace = capacity - currentAmount;
        if (availableSpace <= 0) {
            player.sendMessage(ChatColor.RED + "Barrel is full!");
            return;
        }

        int amountToAdd = Math.min(playerItem.getAmount(), availableSpace);
        int newAmount = currentAmount + amountToAdd;

        // Update stored amount
        PersistentDataAPI.setInt(barrelMeta, amountKey, newAmount);

        // Update barrel appearance
        updateBarrelMeta(barrelMeta, playerItem, newAmount);
        barrel.setItemMeta(barrelMeta);

        // Remove items from player
        if (playerItem.getAmount() <= amountToAdd) {
            playerItem.setAmount(0);
        } else {
            playerItem.setAmount(playerItem.getAmount() - amountToAdd);
        }

        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.0f);
        player.sendMessage(ChatColor.GREEN + "Added " + amountToAdd + "x " +
                getItemDisplayName(playerItem) + " to barrel!");
    }

    private void takeFromBarrel(@Nonnull Player player, @Nonnull ItemStack barrel) {
        ItemMeta barrelMeta = barrel.getItemMeta();
        if (barrelMeta == null) return;

        int storedAmount = PersistentDataAPI.getInt(barrelMeta, amountKey, 0);
        String storedItemData = PersistentDataAPI.getString(barrelMeta, itemKey);

        if (storedAmount <= 0 || storedItemData == null || storedItemData.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Barrel is empty!");
            return;
        }

        ItemStack storedItem = deserializeItem(storedItemData);
        if (storedItem == null) {
            player.sendMessage(ChatColor.RED + "Error: Invalid item data!");
            return;
        }

        // Calculate amount to give
        int maxStackSize = storedItem.getMaxStackSize();
        int amountToGive = player.isSneaking() ? Math.min(maxStackSize, storedAmount) : 1;

        // Create item to give
        ItemStack itemToGive = storedItem.clone();
        itemToGive.setAmount(amountToGive);

        // Check if player has space
        if (!hasInventorySpace(player, itemToGive)) {
            player.sendMessage(ChatColor.RED + "Inventory is full!");
            return;
        }

        // Give item to player
        player.getInventory().addItem(itemToGive);

        // Update barrel
        int newAmount = storedAmount - amountToGive;
        if (newAmount <= 0) {
            // Empty barrel
            PersistentDataAPI.remove(barrelMeta, itemKey);
            PersistentDataAPI.remove(barrelMeta, amountKey);
            resetBarrelMeta(barrelMeta);
        } else {
            PersistentDataAPI.setInt(barrelMeta, amountKey, newAmount);
            updateBarrelMeta(barrelMeta, storedItem, newAmount);
        }

        barrel.setItemMeta(barrelMeta);

        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.0f);
        player.sendMessage(ChatColor.GREEN + "Took " + amountToGive + "x " +
                getItemDisplayName(storedItem) + " from barrel!");
    }

    private void updateBarrelMeta(@Nonnull ItemMeta meta, @Nonnull ItemStack storedItem, int amount) {
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GRAY + "Contains: " + ChatColor.WHITE + getItemDisplayName(storedItem));
        lore.add(ChatColor.GRAY + "Amount: " + ChatColor.WHITE + formatNumber(amount) + "/" + formatNumber(capacity));
        lore.add("");
        lore.add(ChatColor.YELLOW + "Right-click to take 1 item");
        lore.add(ChatColor.YELLOW + "Shift + Right-click to take 1 stack");
        lore.add(ChatColor.YELLOW + "Hold item and right-click to store");

        meta.setLore(lore);
    }

    private void resetBarrelMeta(@Nonnull ItemMeta meta) {
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GRAY + "Empty");
        lore.add(ChatColor.GRAY + "Capacity: " + ChatColor.WHITE + formatNumber(capacity));
        lore.add("");
        lore.add(ChatColor.YELLOW + "Hold item and right-click to store");

        meta.setLore(lore);
    }

    private String formatNumber(int number) {
        if (number >= 1000000) {
            return String.format("%.1fM", number / 1000000.0);
        } else if (number >= 1000) {
            return String.format("%.1fK", number / 1000.0);
        }
        return String.valueOf(number);
    }

    private boolean isValidItem(@Nonnull ItemStack item) {
        // Don't allow storing other barrels
        if (SlimefunItem.getByItem(item) instanceof CustomBarrel) {
            return false;
        }

        // Don't allow air
        if (item.getType() == Material.AIR) {
            return false;
        }

        return true;
    }

    private boolean isSameItem(@Nonnull ItemStack item1, @Nonnull ItemStack item2) {
        return SlimefunUtils.isItemSimilar(item1, item2, true, false);
    }

    private String serializeItem(@Nonnull ItemStack item) {
        try {
            ItemStack clone = item.clone();
            clone.setAmount(1);
            return Base64.getEncoder().encodeToString(clone.serializeAsBytes());
        } catch (Exception e) {
            return item.getType().name();
        }
    }

    @Nullable
    private ItemStack deserializeItem(@Nonnull String data) {
        try {
            byte[] bytes = Base64.getDecoder().decode(data);
            return ItemStack.deserializeBytes(bytes);
        } catch (Exception e) {
            try {
                Material material = Material.valueOf(data);
                return new ItemStack(material, 1);
            } catch (Exception ex) {
                return null;
            }
        }
    }

    private boolean hasInventorySpace(@Nonnull Player player, @Nonnull ItemStack item) {
        return player.getInventory().firstEmpty() != -1 ||
                canStackInExistingSlot(player, item);
    }

    private boolean canStackInExistingSlot(@Nonnull Player player, @Nonnull ItemStack item) {
        for (ItemStack invItem : player.getInventory().getContents()) {
            if (invItem != null && invItem.isSimilar(item)) {
                int space = invItem.getMaxStackSize() - invItem.getAmount();
                if (space >= item.getAmount()) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getItemDisplayName(@Nonnull ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        return item.getType().name().toLowerCase().replace('_', ' ');
    }

    public int getCapacity() {
        return capacity;
    }
}