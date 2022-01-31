package me.fis_chl.vanillaplus.Teleport.Command;

import me.fis_chl.vanillaplus.Teleport.TeleportHandler;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class TptoggleCommand implements CommandExecutor {

    private final TeleportHandler teleportHandler;

    public TptoggleCommand(TeleportHandler teleportHandler) {
        this.teleportHandler = teleportHandler;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof Player) {
            Player player = (Player) src;
            int result = teleportHandler.changePlayerTpData(player);
            if (result == 1) {
                player.sendMessage(
                        Text.builder(
                                "Enabled teleport requests"
                        ).color(TextColors.GREEN).build()
                );
            }
            if (result == 0) {
                player.sendMessage(
                        Text.builder(
                                "Disabled teleport requests"
                        ).color(TextColors.GREEN).build()
                );
            }
        }
        return CommandResult.success();
    }
}
