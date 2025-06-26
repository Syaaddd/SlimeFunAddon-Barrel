package core.sg.barrelAddon.items;

// ====== 4. Custom Barrel Implementation with GUI ======
// src/main/java/com/yourname/barreladdon/items/CustomBarrel.java

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.data.persistent.PersistentDataAPI;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class CustomBarrel extends SlimefunItem {

    private final int capacity;
    private static final int[] BORDER_SLOTS = {0, 1, 2, 3, 5, 6, 7, 8, 9, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26};
    private static final int DISPLAY_SLOT = 4;
    private static final int INPUT_SLOT = 10;
    private static final int OUTPUT_SLOT = 16;
    private static final int INSERT_ALL_SLOT = 11;
    private static final int EXTRACT_ALL_SLOT = 15;

    public CustomBarrel(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType,
                        ItemStack[] recipe, int capacity) {
        super(itemGroup, item, recipeType, recipe);

        this.capacity = capacity;

        addItemHandler(onPlace());
        addItemHandler(onBreak());
        addItemHandler(onBlockUse());
    }

    @Nonnull
    private BlockPlaceHandler onPlace() {
        return new BlockPlaceHandler(false) {
            @Override
            public void onPlayerPlace(@Nonnull BlockPlaceEvent e) {
                Block block = e.getBlock();
                Player player = e.getPlayer();

                // Store barrel data in BlockStorage
                BlockStorage.addBlockInfo(block, "capacity", String.valueOf(capacity));
                BlockStorage.addBlockInfo(block, "stored_amount", "0");
                BlockStorage.addBlockInfo(block, "owner", player.getUniqueId().toString());

                player.sendMessage(ChatColor.GREEN + "Barrel placed successfully!");
            }
        };
    }

    @Nonnull
    private BlockBreakHandler onBreak() {
        return new BlockBreakHandler(false, false) {
            @Override
            public void onPlayerBreak(@Nonnull BlockBreakEvent e, @Nonnull ItemStack item, @Nonnull List<ItemStack> drops) {
                Block block = e.getBlock();
                Player player = e.getPlayer();
                Location location = block.getLocation();

                // Get stored data
                String storedItemData = BlockStorage.getLocationInfo(location, "stored_item");
                String storedAmountStr = BlockStorage.getLocationInfo(location, "stored_amount");

                // Clear the default drops to prevent double drop
                drops.clear();

                // Drop stored items first
                if (storedItemData != null && !storedItemData.isEmpty() && storedAmountStr != null) {
                    try {
                        int storedAmount = Integer.parseInt(storedAmountStr);
                        if (storedAmount > 0) {
                            ItemStack storedItem = deserializeItem(storedItemData);
                            if (storedItem != null) {
                                // Drop stored items
                                dropStoredItems(location, storedItem, storedAmount);
                                player.sendMessage(ChatColor.YELLOW + "Dropped " + storedAmount + "x " +
                                        getItemDisplayName(storedItem) + " from barrel!");
                            }
                        }
                    } catch (NumberFormatException ignored) {}
                }

                // Create and drop barrel item
                ItemStack barrelItem = getItem().clone();
                location.getWorld().dropItemNaturally(location, barrelItem);

                // Clear block storage data
                BlockStorage.clearBlockInfo(block);
            }
        };
    }

    @Nonnull
    private BlockUseHandler onBlockUse() {
        return e -> {
            e.cancel();

            Player player = e.getPlayer();
            Block block = e.getClickedBlock().get();

            // Open GUI
            openBarrelGUI(player, block);
        };
    }

    private void openBarrelGUI(@Nonnull Player player, @Nonnull Block block) {
        ChestMenu menu = new ChestMenu("&6Barrel Storage");

        // Make player inventory clickable
        menu.setPlayerInventoryClickable(true);

        // Set up border (but leave input slot interactive)
        for (int slot : BORDER_SLOTS) {
            menu.addItem(slot, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }

        // Update display and setup menu
        updateGUIDisplay(menu, block);
        setupGUIHandlers(menu, block, player);

        menu.open(player);
    }

    private void updateGUIDisplay(@Nonnull ChestMenu menu, @Nonnull Block block) {
        Location loc = block.getLocation();
        String storedItemData = BlockStorage.getLocationInfo(loc, "stored_item");
        String storedAmountStr = BlockStorage.getLocationInfo(loc, "stored_amount");

        int storedAmount = 0;
        try {
            storedAmount = storedAmountStr != null ? Integer.parseInt(storedAmountStr) : 0;
        } catch (NumberFormatException e) {
            storedAmount = 0;
        }

        // Display slot
        ItemStack displayItem;
        if (storedAmount > 0 && storedItemData != null && !storedItemData.isEmpty()) {
            ItemStack storedItem = deserializeItem(storedItemData);
            if (storedItem != null) {
                displayItem = new CustomItemStack(storedItem.getType(),
                        "&e" + getItemDisplayName(storedItem),
                        "",
                        "&7Amount: &f" + formatNumber(storedAmount) + "&7/&f" + formatNumber(capacity),
                        "&7Capacity: &f" + String.format("%.1f%%", (storedAmount * 100.0) / capacity),
                        "",
                        "&eClick output slot to take items"
                );

                if (storedItem.hasItemMeta()) {
                    displayItem.setItemMeta(storedItem.getItemMeta());
                }
            } else {
                displayItem = new CustomItemStack(Material.BARRIER,
                        "&cError",
                        "&7Invalid item data"
                );
            }
        } else {
            displayItem = new CustomItemStack(Material.GRAY_STAINED_GLASS_PANE,
                    "&7Empty Barrel",
                    "",
                    "&7Capacity: &f" + formatNumber(capacity),
                    "",
                    "&ePut items in the input slot"
            );
        }

        menu.replaceExistingItem(DISPLAY_SLOT, displayItem);

        // Input slot - Create an empty slot that accepts items
        menu.replaceExistingItem(INPUT_SLOT, null);

        // Output slot
        menu.replaceExistingItem(OUTPUT_SLOT, new CustomItemStack(Material.RED_STAINED_GLASS_PANE,
                "&cOutput Slot",
                "&7Click to take items",
                "&7Shift-click for full stack"
        ));

        // Insert All button
        menu.replaceExistingItem(INSERT_ALL_SLOT, new CustomItemStack(Material.GREEN_STAINED_GLASS_PANE,
                "&aInsert All",
                "&7Click to insert all matching items",
                "&7from your inventory"
        ));

        // Extract All button
        menu.replaceExistingItem(EXTRACT_ALL_SLOT, new CustomItemStack(Material.ORANGE_STAINED_GLASS_PANE,
                "&6Extract All",
                "&7Click to extract all items",
                "&7from the barrel"
        ));
    }

    private void setupGUIHandlers(@Nonnull ChestMenu menu, @Nonnull Block block, @Nonnull Player player) {
        // Input slot handler - Allow placing items from inventory
        menu.addMenuClickHandler(INPUT_SLOT, (p, slot, item, action) -> {
            ItemStack cursor = p.getItemOnCursor();
            ItemStack slotItem = menu.getItemInSlot(slot);

            // Handle placing item from cursor
            if (cursor != null && cursor.getType() != Material.AIR) {
                if (!isValidItem(cursor)) {
                    p.sendMessage(ChatColor.RED + "This item cannot be stored in the barrel!");
                    return false;
                }

                if (storeItemFromCursor(p, block, cursor)) {
                    updateGUIDisplay(menu, block);
                    p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.0f);
                }
                return false;
            }

            // Handle clicking item in slot
            if (slotItem != null && slotItem.getType() != Material.AIR && isValidItem(slotItem)) {
                if (storeClickedItem(p, block, slotItem)) {
                    menu.replaceExistingItem(slot, null);
                    updateGUIDisplay(menu, block);
                    p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.0f);
                }
                return false;
            }

            return true; // Allow normal inventory operations
        });

        // Handle shift-clicking from player inventory to input slot
        menu.addMenuClickHandler(-1, (p, slot, item, action) -> {
            if (action.toString().contains("SHIFT") && item != null && isValidItem(item)) {
                if (storeItemFromInventory(p, block, item, slot)) {
                    updateGUIDisplay(menu, block);
                    p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.0f);
                }
                return false;
            }
            return true;
        });

        // Make sure player inventory is clickable
        menu.setPlayerInventoryClickable(true);

        // Output slot handler
        menu.addMenuClickHandler(OUTPUT_SLOT, (p, slot, item, action) -> {
            boolean shiftClick = action.isShiftClicked();
            if (takeItemFromGUI(p, block, shiftClick)) {
                updateGUIDisplay(menu, block);
                p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.0f);
            }
            return false;
        });

        // Insert All button handler
        menu.addMenuClickHandler(INSERT_ALL_SLOT, (p, slot, item, action) -> {
            if (insertAllMatchingItems(p, block)) {
                updateGUIDisplay(menu, block);
                p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.0f);
            }
            return false;
        });

        // Extract All button handler
        menu.addMenuClickHandler(EXTRACT_ALL_SLOT, (p, slot, item, action) -> {
            if (extractAllItems(p, block)) {
                updateGUIDisplay(menu, block);
                p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.0f);
            }
            return false;
        });
    }

    private boolean storeItemFromInventory(@Nonnull Player player, @Nonnull Block block, @Nonnull ItemStack invItem, int invSlot) {
        Location loc = block.getLocation();
        String storedItemData = BlockStorage.getLocationInfo(loc, "stored_item");
        String storedAmountStr = BlockStorage.getLocationInfo(loc, "stored_amount");

        int storedAmount = 0;
        try {
            storedAmount = storedAmountStr != null ? Integer.parseInt(storedAmountStr) : 0;
        } catch (NumberFormatException e) {
            storedAmount = 0;
        }

        // Check if barrel is empty or contains same item
        if (storedAmount == 0 || storedItemData == null || storedItemData.isEmpty()) {
            // Empty barrel - store new item
            int amountToStore = Math.min(invItem.getAmount(), capacity);

            String itemData = serializeItem(invItem);
            BlockStorage.addBlockInfo(block, "stored_item", itemData);
            BlockStorage.addBlockInfo(block, "stored_amount", String.valueOf(amountToStore));

            // Update inventory
            if (amountToStore == invItem.getAmount()) {
                player.getInventory().setItem(invSlot, null);
            } else {
                invItem.setAmount(invItem.getAmount() - amountToStore);
            }

            player.sendMessage(ChatColor.GREEN + "Stored " + amountToStore + "x " +
                    getItemDisplayName(invItem) + " in barrel!");
            return true;

        } else {
            // Check if same item type
            ItemStack storedItem = deserializeItem(storedItemData);
            if (storedItem != null && isSameItem(storedItem, invItem)) {
                int availableSpace = capacity - storedAmount;
                if (availableSpace <= 0) {
                    player.sendMessage(ChatColor.RED + "Barrel is full!");
                    return false;
                }

                int amountToAdd = Math.min(invItem.getAmount(), availableSpace);
                int newAmount = storedAmount + amountToAdd;

                BlockStorage.addBlockInfo(block, "stored_amount", String.valueOf(newAmount));

                // Update inventory
                if (amountToAdd == invItem.getAmount()) {
                    player.getInventory().setItem(invSlot, null);
                } else {
                    invItem.setAmount(invItem.getAmount() - amountToAdd);
                }

                player.sendMessage(ChatColor.GREEN + "Added " + amountToAdd + "x " +
                        getItemDisplayName(invItem) + " to barrel!");
                return true;
            } else {
                player.sendMessage(ChatColor.RED + "Barrel already contains a different item!");
                return false;
            }
        }
    }

    private boolean storeClickedItem(@Nonnull Player player, @Nonnull Block block, @Nonnull ItemStack clickedItem) {
        Location loc = block.getLocation();
        String storedItemData = BlockStorage.getLocationInfo(loc, "stored_item");
        String storedAmountStr = BlockStorage.getLocationInfo(loc, "stored_amount");

        int storedAmount = 0;
        try {
            storedAmount = storedAmountStr != null ? Integer.parseInt(storedAmountStr) : 0;
        } catch (NumberFormatException e) {
            storedAmount = 0;
        }

        // Check if barrel is empty or contains same item
        if (storedAmount == 0 || storedItemData == null || storedItemData.isEmpty()) {
            // Empty barrel - store new item
            int amountToStore = Math.min(clickedItem.getAmount(), capacity);

            String itemData = serializeItem(clickedItem);
            BlockStorage.addBlockInfo(block, "stored_item", itemData);
            BlockStorage.addBlockInfo(block, "stored_amount", String.valueOf(amountToStore));

            player.sendMessage(ChatColor.GREEN + "Stored " + amountToStore + "x " +
                    getItemDisplayName(clickedItem) + " in barrel!");
            return true;

        } else {
            // Check if same item type
            ItemStack storedItem = deserializeItem(storedItemData);
            if (storedItem != null && isSameItem(storedItem, clickedItem)) {
                int availableSpace = capacity - storedAmount;
                if (availableSpace <= 0) {
                    player.sendMessage(ChatColor.RED + "Barrel is full!");
                    return false;
                }

                int amountToAdd = Math.min(clickedItem.getAmount(), availableSpace);
                int newAmount = storedAmount + amountToAdd;

                BlockStorage.addBlockInfo(block, "stored_amount", String.valueOf(newAmount));

                player.sendMessage(ChatColor.GREEN + "Added " + amountToAdd + "x " +
                        getItemDisplayName(clickedItem) + " to barrel!");
                return true;
            } else {
                player.sendMessage(ChatColor.RED + "Barrel already contains a different item!");
                return false;
            }
        }
    }

    private boolean storeItemFromCursor(@Nonnull Player player, @Nonnull Block block, @Nonnull ItemStack cursorItem) {
        Location loc = block.getLocation();
        String storedItemData = BlockStorage.getLocationInfo(loc, "stored_item");
        String storedAmountStr = BlockStorage.getLocationInfo(loc, "stored_amount");

        int storedAmount = 0;
        try {
            storedAmount = storedAmountStr != null ? Integer.parseInt(storedAmountStr) : 0;
        } catch (NumberFormatException e) {
            storedAmount = 0;
        }

        // Check if barrel is empty or contains same item
        if (storedAmount == 0 || storedItemData == null || storedItemData.isEmpty()) {
            // Empty barrel - store new item
            int amountToStore = Math.min(cursorItem.getAmount(), capacity);

            String itemData = serializeItem(cursorItem);
            BlockStorage.addBlockInfo(block, "stored_item", itemData);
            BlockStorage.addBlockInfo(block, "stored_amount", String.valueOf(amountToStore));

            // Update cursor item
            int remaining = cursorItem.getAmount() - amountToStore;
            if (remaining > 0) {
                cursorItem.setAmount(remaining);
                player.setItemOnCursor(cursorItem);
            } else {
                player.setItemOnCursor(null);
            }

            player.sendMessage(ChatColor.GREEN + "Stored " + amountToStore + "x " +
                    getItemDisplayName(cursorItem) + " in barrel!");
            return true;

        } else {
            // Check if same item type
            ItemStack storedItem = deserializeItem(storedItemData);
            if (storedItem != null && isSameItem(storedItem, cursorItem)) {
                int availableSpace = capacity - storedAmount;
                if (availableSpace <= 0) {
                    player.sendMessage(ChatColor.RED + "Barrel is full!");
                    return false;
                }

                int amountToAdd = Math.min(cursorItem.getAmount(), availableSpace);
                int newAmount = storedAmount + amountToAdd;

                BlockStorage.addBlockInfo(block, "stored_amount", String.valueOf(newAmount));

                // Update cursor item
                int remaining = cursorItem.getAmount() - amountToAdd;
                if (remaining > 0) {
                    cursorItem.setAmount(remaining);
                    player.setItemOnCursor(cursorItem);
                } else {
                    player.setItemOnCursor(null);
                }

                player.sendMessage(ChatColor.GREEN + "Added " + amountToAdd + "x " +
                        getItemDisplayName(cursorItem) + " to barrel!");
                return true;
            } else {
                player.sendMessage(ChatColor.RED + "Barrel already contains a different item!");
                return false;
            }
        }
    }

    private boolean insertAllMatchingItems(@Nonnull Player player, @Nonnull Block block) {
        Location loc = block.getLocation();
        String storedItemData = BlockStorage.getLocationInfo(loc, "stored_item");
        String storedAmountStr = BlockStorage.getLocationInfo(loc, "stored_amount");

        int storedAmount = 0;
        try {
            storedAmount = storedAmountStr != null ? Integer.parseInt(storedAmountStr) : 0;
        } catch (NumberFormatException e) {
            storedAmount = 0;
        }

        // If barrel is empty, find the first valid item in inventory
        if (storedAmount == 0) {
            for (ItemStack invItem : player.getInventory().getContents()) {
                if (invItem != null && isValidItem(invItem)) {
                    // Store this item type
                    String itemData = serializeItem(invItem);
                    BlockStorage.addBlockInfo(block, "stored_item", itemData);
                    storedItemData = itemData;
                    break;
                }
            }
            if (storedItemData == null) {
                player.sendMessage(ChatColor.RED + "No valid items found in inventory!");
                return false;
            }
        }

        ItemStack storedItem = deserializeItem(storedItemData);
        if (storedItem == null) {
            player.sendMessage(ChatColor.RED + "Error: Invalid item data!");
            return false;
        }

        int totalInserted = 0;
        int availableSpace = capacity - storedAmount;

        // Go through inventory and collect matching items
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack invItem = player.getInventory().getItem(i);
            if (invItem != null && isSameItem(storedItem, invItem) && availableSpace > 0) {
                int amountToTake = Math.min(invItem.getAmount(), availableSpace);
                totalInserted += amountToTake;
                availableSpace -= amountToTake;

                if (amountToTake == invItem.getAmount()) {
                    player.getInventory().setItem(i, null);
                } else {
                    invItem.setAmount(invItem.getAmount() - amountToTake);
                }
            }
        }

        if (totalInserted > 0) {
            int newAmount = storedAmount + totalInserted;
            BlockStorage.addBlockInfo(block, "stored_amount", String.valueOf(newAmount));
            player.sendMessage(ChatColor.GREEN + "Inserted " + totalInserted + "x " +
                    getItemDisplayName(storedItem) + " into barrel!");
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "No matching items found or barrel is full!");
            return false;
        }
    }

    private boolean extractAllItems(@Nonnull Player player, @Nonnull Block block) {
        Location loc = block.getLocation();
        String storedAmountStr = BlockStorage.getLocationInfo(loc, "stored_amount");
        String storedItemData = BlockStorage.getLocationInfo(loc, "stored_item");

        int storedAmount = 0;
        try {
            storedAmount = storedAmountStr != null ? Integer.parseInt(storedAmountStr) : 0;
        } catch (NumberFormatException e) {
            storedAmount = 0;
        }

        if (storedAmount <= 0 || storedItemData == null || storedItemData.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Barrel is empty!");
            return false;
        }

        ItemStack storedItem = deserializeItem(storedItemData);
        if (storedItem == null) {
            player.sendMessage(ChatColor.RED + "Error: Invalid item data!");
            return false;
        }

        int totalExtracted = 0;
        int remaining = storedAmount;

        // Give items in stacks
        while (remaining > 0) {
            int stackSize = Math.min(remaining, storedItem.getMaxStackSize());
            ItemStack itemToGive = storedItem.clone();
            itemToGive.setAmount(stackSize);

            if (hasInventorySpace(player, itemToGive)) {
                player.getInventory().addItem(itemToGive);
                totalExtracted += stackSize;
                remaining -= stackSize;
            } else {
                break; // Inventory full
            }
        }

        if (totalExtracted > 0) {
            int newAmount = storedAmount - totalExtracted;
            if (newAmount <= 0) {
                // Empty barrel
                BlockStorage.addBlockInfo(block, "stored_item", "");
                BlockStorage.addBlockInfo(block, "stored_amount", "0");
            } else {
                BlockStorage.addBlockInfo(block, "stored_amount", String.valueOf(newAmount));
            }

            player.sendMessage(ChatColor.GREEN + "Extracted " + totalExtracted + "x " +
                    getItemDisplayName(storedItem) + " from barrel!");

            if (totalExtracted < storedAmount) {
                player.sendMessage(ChatColor.YELLOW + "Inventory full! " +
                        (storedAmount - totalExtracted) + " items remaining in barrel.");
            }
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "Inventory is full!");
            return false;
        }
    }

    private boolean takeItemFromGUI(@Nonnull Player player, @Nonnull Block block, boolean fullStack) {
        Location loc = block.getLocation();
        String storedAmountStr = BlockStorage.getLocationInfo(loc, "stored_amount");
        String storedItemData = BlockStorage.getLocationInfo(loc, "stored_item");

        int storedAmount = 0;
        try {
            storedAmount = storedAmountStr != null ? Integer.parseInt(storedAmountStr) : 0;
        } catch (NumberFormatException e) {
            storedAmount = 0;
        }

        if (storedAmount <= 0 || storedItemData == null || storedItemData.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Barrel is empty!");
            return false;
        }

        ItemStack storedItem = deserializeItem(storedItemData);
        if (storedItem == null) {
            player.sendMessage(ChatColor.RED + "Error: Invalid item data!");
            return false;
        }

        // Calculate amount to give
        int maxStackSize = storedItem.getMaxStackSize();
        int amountToGive = fullStack ? Math.min(maxStackSize, storedAmount) : 1;

        // Create item to give
        ItemStack itemToGive = storedItem.clone();
        itemToGive.setAmount(amountToGive);

        // Check if player has space
        if (!hasInventorySpace(player, itemToGive)) {
            player.sendMessage(ChatColor.RED + "Inventory is full!");
            return false;
        }

        // Give item to player
        player.getInventory().addItem(itemToGive);

        // Update barrel
        int newAmount = storedAmount - amountToGive;
        if (newAmount <= 0) {
            // Empty barrel
            BlockStorage.addBlockInfo(block, "stored_item", "");
            BlockStorage.addBlockInfo(block, "stored_amount", "0");
        } else {
            BlockStorage.addBlockInfo(block, "stored_amount", String.valueOf(newAmount));
        }

        player.sendMessage(ChatColor.GREEN + "Took " + amountToGive + "x " +
                getItemDisplayName(storedItem) + " from barrel!");
        return true;
    }

    private void dropStoredItems(@Nonnull Location location, @Nonnull ItemStack item, int amount) {
        int remaining = amount;
        while (remaining > 0) {
            int dropAmount = Math.min(remaining, item.getMaxStackSize());
            ItemStack dropItem = item.clone();
            dropItem.setAmount(dropAmount);
            location.getWorld().dropItemNaturally(location, dropItem);
            remaining -= dropAmount;
        }
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

        // Don't allow GUI items
        if (item.getType() == Material.LIME_STAINED_GLASS_PANE ||
                item.getType() == Material.RED_STAINED_GLASS_PANE ||
                item.getType() == Material.GRAY_STAINED_GLASS_PANE ||
                item.getType() == Material.GREEN_STAINED_GLASS_PANE ||
                item.getType() == Material.ORANGE_STAINED_GLASS_PANE) {
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
        if (data == null || data.isEmpty()) {
            return null;
        }

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