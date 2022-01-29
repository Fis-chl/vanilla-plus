package me.fis_chl.vanillaplus.Command;

import me.fis_chl.vanillaplus.Teleport.TeleportHandler;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class TpcancelCommand implements CommandExecutor {

    private final TeleportHandler teleportHandler;

    public TpcancelCommand(TeleportHandler teleportHandler) {
        this.teleportHandler = teleportHandler;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof Player) {
            Player player = (Player) src;
            boolean result = teleportHandler.cancelRequest(player.getUniqueId());
            if (result) {
                player.sendMessage(
                        Text.builder(
                                "Cancelled request successfully"
                        ).color(TextColors.GREEN).build()
                );
            } else {
                player.sendMessage(
                        Text.builder(
                                "You don't have any pending teleport requests!"
                        ).color(TextColors.RED).build()
                );
            }
        }
        return CommandResult.success();
    }
}
