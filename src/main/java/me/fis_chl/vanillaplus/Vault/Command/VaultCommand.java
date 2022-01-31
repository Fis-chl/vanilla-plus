package me.fis_chl.vanillaplus.Vault.Command;

import me.fis_chl.vanillaplus.Vault.Vault;
import me.fis_chl.vanillaplus.Vault.VaultHandler;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;

public class VaultCommand implements CommandExecutor {

    private final VaultHandler vaultHandler;

    public VaultCommand(VaultHandler vaultHandler) {
        this.vaultHandler = vaultHandler;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof Player) {
            Player player = (Player) src;
            vaultHandler.createVault(player);
            player.openInventory(vaultHandler.buildInventoryFromVault(player));
        }
        return null;
    }
}
