package me.fis_chl.vanillaplus.Vault.Command;

import me.fis_chl.vanillaplus.Vault.VaultHandler;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Inventory;

public class VaultUpgradeCommand implements CommandExecutor {

    private final VaultHandler vaultHandler;

    public VaultUpgradeCommand(VaultHandler vaultHandler) {
        this.vaultHandler = vaultHandler;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof Player) {
            Player player = (Player) src;
            Inventory vault = vaultHandler.buildInventoryFromVault(player);
            if (vault != null) {
                player.openInventory(vault);
            }
        }
        return CommandResult.success();
    }
}
