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

public class TpdenyCommand implements CommandExecutor {

    private final TeleportHandler teleportHandler;

    public TpdenyCommand(TeleportHandler teleportHandler) {
        this.teleportHandler = teleportHandler;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof Player) {
            Player pDestination = (Player) src;
            boolean hasRequests = teleportHandler.denyRequest(pDestination.getUniqueId());
            if (!hasRequests) {
                pDestination.sendMessage(
                        Text.builder(
                                "There are no pending teleport requests!")
                                .color(TextColors.RED)
                                .build()
                );
            }
        }
        return CommandResult.success();
    }
}
