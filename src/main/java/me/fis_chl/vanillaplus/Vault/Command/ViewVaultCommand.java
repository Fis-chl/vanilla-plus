package me.fis_chl.vanillaplus.Vault.Command;

import me.fis_chl.vanillaplus.Vault.VaultHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class ViewVaultCommand implements CommandExecutor {

    private final VaultHandler vaultHandler;

    public ViewVaultCommand(VaultHandler vaultHandler) {
        this.vaultHandler = vaultHandler;
    }
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof Player) {
            Player player = (Player) src;
            Player toView = args.requireOne(Text.of("player"));
            if (player.getUniqueId().equals(toView.getUniqueId())) {
                player.sendMessage(Text.builder(
                        "Access your own vault using /vault"
                ).color(TextColors.AQUA).build());
            }
            PluginContainer plugin = Sponge.getPluginManager().getPlugin("vanillaplus").get();
            Inventory vault = Inventory.builder().from(vaultHandler.buildInventoryFromVault(toView))
                    .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(Text.builder(
                            toView.getName() + "'s Vault"
                    ).color(TextColors.RED).build())).build(plugin);
            player.openInventory(vault);
        }
        return CommandResult.success();
    }
}
