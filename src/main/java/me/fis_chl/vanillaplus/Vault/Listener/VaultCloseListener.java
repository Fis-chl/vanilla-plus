package me.fis_chl.vanillaplus.Vault.Listener;

import me.fis_chl.vanillaplus.Vault.VaultHandler;
import org.slf4j.Logger;
import org.spongepowered.api.block.tileentity.carrier.Furnace;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class VaultCloseListener implements EventListener<InteractInventoryEvent.Close> {

    private final VaultHandler vaultHandler;
    private final Logger logger;

    public VaultCloseListener(VaultHandler vaultHandler, Logger logger) {
        this.vaultHandler = vaultHandler;
        this.logger = logger;
    }

    @Override
    public void handle(InteractInventoryEvent.Close event) throws Exception {
        Player player = event.getCause().first(Player.class).orElse(null);
        if (player != null) {
            Inventory inventory = event.getTargetInventory();
            Optional<InventoryTitle> oInvTitle = inventory.getProperty(InventoryTitle.class, InventoryTitle.PROPERTY_NAME);
            if (oInvTitle.isPresent()) {
                // This is a player vault
                if (oInvTitle.get().equals(InventoryTitle.of(Text.builder(
                        "Vault").color(TextColors.LIGHT_PURPLE).build()))) {
                    vaultHandler.updateVaultContents(player, inventory);
                }
            }
        }
    }
}
