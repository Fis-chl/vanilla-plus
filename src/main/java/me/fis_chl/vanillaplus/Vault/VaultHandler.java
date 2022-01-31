package me.fis_chl.vanillaplus.Vault;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.*;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.extent.Extent;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VaultHandler {

    private final Path configDir;
    private final Logger logger;
    private final List<Vault> vaults;

    public VaultHandler(Path configDir, Logger logger) {
        this.configDir = configDir;
        this.logger = logger;
        vaults = new ArrayList<>();
        // TODO load vaults
    }

    public void createVault(Player player) {
        Vault vault = getVault(player.getUniqueId());
        if (vault == null) {
            vault = new Vault(player.getUniqueId(), new ArrayList<>(), 0);
            vaults.add(vault);
        }
    }

    public Inventory buildInventoryFromVault(Player player) {
        Vault vault = getVault(player.getUniqueId());
        if (vault != null) {
            // Build an inventory from the vault items
            PluginContainer plugin = Sponge.getPluginManager().getPlugin("vanillaplus").get();
            Inventory inventory = Inventory.builder()
                    .property(InventoryDimension.PROPERTY_NAME, InventoryDimension.of(9, 3 + (vault.getTier())))
                    .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(
                            Text.builder(
                            "Vault").color(TextColors.LIGHT_PURPLE).build()))
                    .build(plugin);
            int counter = 0;
            List<ItemStackSnapshot> vaultItems = vault.getVaultItems();
            for (Inventory slot : inventory.slots()) {
                try {
                    slot.set(vaultItems.get(counter).createStack());
                } catch (IndexOutOfBoundsException e) {
                    slot.set(ItemStack.of(ItemTypes.AIR));
                }
                counter += 1;
            }
            return inventory;
        } else {
            return null;
        }
    }

    public void updateVaultContents(Player player, Inventory vaultInventory) {
        Vault vault = getVault(player.getUniqueId());
        if (vault != null) {
            List<ItemStackSnapshot> inventoryItems = new ArrayList<>();
            for (Inventory slot : vaultInventory.slots()) {
                inventoryItems.add(slot.peek().orElse(ItemStack.of(ItemTypes.AIR, 1)).createSnapshot());
            }
            vault.setVaultItems(inventoryItems);
        } else {
            logger.error("Unable to get vault for player " + player.getName() + " - despite them opening one");
        }
    }

    public Inventory buildUpgradeInventory(Player player) {
        Vault vault = getVault(player.getUniqueId());
        if (vault == null) {
            createVault(player);
            vault = getVault(player.getUniqueId());
        }
        if (vault.getTier() == 3) {
            player.sendMessage(
                    Text.builder(
                            "Your vault is already at max level!"
                    ).color(TextColors.RED).build()
            );
            return null;
        }
        Text invTitle = Text.builder(
                "Vault Upgrade: "
        ).color(TextColors.LIGHT_PURPLE).build();
        Text tier = Text.builder(
                "Tier 0 -> Tier 1").color(TextColors.GREEN).build();
        if (vault.getTier() == 1) {
            tier = Text.builder(
                    "Tier 1 -> Tier 2").color(TextColors.GOLD).build();
        } else if (vault.getTier() == 2) {
            tier = Text.builder(
                    "Tier 2 -> Tier 3").color(TextColors.RED).build();
        }
        invTitle = Text.join(invTitle, tier);
        PluginContainer plugin = Sponge.getPluginManager().getPlugin("vanillaplus").get();
        Inventory inventory = Inventory.builder()
                .property(InventoryDimension.PROPERTY_NAME, InventoryDimension.of(9, 1))
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(invTitle))
                .listener(ClickInventoryEvent.class, clickInventoryEvent -> {
                    // This code is SUPER messy
                    // TODO Clean up this code
                    if (clickInventoryEvent.getSlot().isPresent()) { // Check that a slot was actually clicked
                        Slot slot = clickInventoryEvent.getSlot().get();
                        Vault pv = getVault(clickInventoryEvent.getCause().first(Player.class).get().getUniqueId());
                        if (!(clickInventoryEvent instanceof ClickInventoryEvent.Primary || clickInventoryEvent instanceof ClickInventoryEvent.Secondary)) { // Prevent illegal clicks
                            clickInventoryEvent.setCancelled(true);
                        }
                        if (!slot.peek().isPresent()) {  // Check if the clicked slot has an item in it
                            ItemStackSnapshot cursorItem = clickInventoryEvent.getCursorTransaction().getFinal();
                            ItemStack slotStack = cursorItem.createStack();
                            if (slotStack.get(Keys.DISPLAY_NAME).isPresent()) {  // Check if the player is clicking the upgrade button
                                if (slotStack.get(Keys.DISPLAY_NAME).get().equals(
                                        Text.builder("Click to upgrade!"
                                        ).color(TextColors.GREEN).build())) {
                                    // Refund some amount if the player put in too many diamond blocks
                                    ItemStack returnStack = ItemStack.of(ItemTypes.DIAMOND_BLOCK);
                                    if (slotStack.getQuantity() > (1 + (pv.getTier() * 5))) {  // Drop the item on the player in case their inv is full
                                        returnStack.setQuantity(slotStack.getQuantity() - (1 + (pv.getTier() * 5)));
                                        Extent extent = player.getWorld();
                                        Entity item = extent.createEntity(EntityTypes.ITEM, player.getPosition());
                                        item.offer(Keys.REPRESENTED_ITEM, returnStack.createSnapshot());
                                        extent.spawnEntity(item);
                                    }
                                    pv.setTier(pv.getTier() + 1);  // Increment tier
                                    player.sendMessage(Text.builder(
                                            "Your vault was upgraded to the next tier!"
                                    ).color(TextColors.GREEN).build());
                                    player.closeInventory();
                                    clickInventoryEvent.setCancelled(true);
                                }
                            }
                        }
                        // Handle clicking in the rest of the inventory
                        int clickedIndex = getClickedSlotIndex(clickInventoryEvent.getTargetInventory(), slot);
                        if (clickedIndex < 9 && clickedIndex != 4) {
                            clickInventoryEvent.setCancelled(true);
                        } else { // If the right number of diamond blocks are in the middle slot
                            ItemStack itemInSlot = getSlotAt(clickInventoryEvent.getTargetInventory(), 4);
                            if (itemInSlot != null && itemInSlot.getType() == ItemTypes.DIAMOND_BLOCK && itemInSlot.getQuantity() >= 1 + (pv.getTier() * 5)) {
                                setSlot(getSlot(clickInventoryEvent.getTargetInventory(), 6), ItemStack.builder()
                                        .itemType(ItemTypes.STAINED_GLASS_PANE)
                                        .add(Keys.DYE_COLOR, DyeColors.GREEN)
                                        .add(Keys.DISPLAY_NAME, Text.builder(
                                                "Click to upgrade!"
                                        ).color(TextColors.GREEN).build()).build());
                            } else {  // If the incorrect item is in the middle slot
                                setSlot(getSlot(clickInventoryEvent.getTargetInventory(), 6),
                                        ItemStack.builder().add(Keys.DISPLAY_NAME,
                                                        Text.join(
                                                                Text.builder(
                                                                        "Requires ").color(TextColors.RED).build(),
                                                                Text.builder(
                                                                        "" + (1 + (pv.getTier() * 5))).color(TextColors.GOLD).build(),
                                                                Text.builder(
                                                                        " Diamond Blocks").color(TextColors.AQUA).build()
                                                        ))
                                                .itemType(ItemTypes.STAINED_GLASS_PANE)
                                                .add(Keys.DYE_COLOR, DyeColors.RED)
                                                .quantity(1).build());
                            }
                        }
                    }
                })
                .build(plugin);
        int counter = 0;
        for (Inventory slot : inventory.slots()) {
            if (counter == 2) {
                slot.set(ItemStack.builder()
                        .itemType(ItemTypes.DIAMOND_BLOCK)
                        .add(Keys.DISPLAY_NAME, Text.builder(
                                "Required Diamond Blocks: " + (1 + (vault.getTier() * 5)
                        )).color(TextColors.AQUA).build())
                        .build());
            } else if (counter == 4) {
                slot.set(ItemStack.of(ItemTypes.AIR));
            } else if (counter == 6) {
                slot.set(
                        ItemStack.builder().add(Keys.DISPLAY_NAME,
                                Text.join(
                                        Text.builder(
                                                "Requires ").color(TextColors.RED).build(),
                                        Text.builder(
                                                "" + (1 + (vault.getTier() * 5))).color(TextColors.GOLD).build(),
                                        Text.builder(
                                                " Diamond Blocks").color(TextColors.AQUA).build()
                                ))
                                .itemType(ItemTypes.STAINED_GLASS_PANE)
                                .add(Keys.DYE_COLOR, DyeColors.RED)
                                .quantity(1).build()
                );
            } else {
                slot.set(ItemStack.builder()
                        .itemType(ItemTypes.STAINED_GLASS_PANE)
                        .add(Keys.DYE_COLOR, DyeColors.RED)
                        .add(Keys.DISPLAY_NAME, Text.of(" "))
                        .quantity(1)
                        .build());
            }
            counter++;
        }
        logger.info("Upgrade inventory built!");
        return inventory;
    }

    private ItemStack getSlotAt(Inventory inventory, int index) {
        int counter = 0;
        for (Inventory inv : inventory.slots()) {
            if (counter == index) {
                return inv.peek().orElse(null);
            }
            counter++;
        }
        return null;
    }

    private Inventory getSlot(Inventory inventory, int index) {
        int counter = 0;
        for (Inventory inv : inventory.slots()) {
            if (counter == index) {
                return inv;
            }
            counter++;
        }
        return null;
    }

    private int getClickedSlotIndex(Inventory inventory, Inventory slot) {
        int counter = 0;
        for (Inventory inv : inventory.slots()) {
            if (inv.equals(slot)) {
                return counter;
            }
            counter++;
        }
        return -1;
    }

    private void setSlot(Inventory slot, ItemStack toSetAs) {
        slot.set(toSetAs);
    }

    public Vault getVault(UUID ownerId) {
        for (Vault vault : vaults) {
            if (vault.getOwnerId().equals(ownerId)) {
                return vault;
            }
        }
        return null;
    }
}
