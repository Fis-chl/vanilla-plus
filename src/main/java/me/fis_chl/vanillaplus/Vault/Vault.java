package me.fis_chl.vanillaplus.Vault;

import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.List;
import java.util.UUID;

public class Vault {

    private final UUID ownerId;
    private List<ItemStackSnapshot> vaultItems;
    private int tier;

    public Vault(UUID ownerId, List<ItemStackSnapshot> vaultItems, int tier) {
        this.ownerId = ownerId;
        this.vaultItems = vaultItems;
        this.tier = tier;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public List<ItemStackSnapshot> getVaultItems() {
        return vaultItems;
    }

    public void setVaultItems(List<ItemStackSnapshot> vaultItems) {
        this.vaultItems = vaultItems;
    }

    public int getTier() {
        return tier;
    }

    public void setTier(int tier) {
        this.tier = tier;
    }
}
