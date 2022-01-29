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

public class TpaCommand implements CommandExecutor {

    private final TeleportHandler teleportHandler;

    public TpaCommand(TeleportHandler teleportHandler) {
        this.teleportHandler = teleportHandler;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof Player) {  // Ensure that a player sends the command
            Player requester = (Player) src;
            Player destination = (Player) args.getOne("destinationPlayer").orElse(null);
            if (requester.equals(destination)) {
                requester.sendMessage(
                        Text.builder(
                                "You can't send a teleport request to yourself!"
                        ).color(TextColors.RED).build()
                );
            } else {
                if (destination != null) {  // Checks that there is a destination argument
                    int result = teleportHandler.createRequest(requester, destination);
                    if (result == 0) {
                        requester.sendMessage(
                                Text.builder(
                                        "You already have a pending teleport request to this player!")
                                        .color(TextColors.RED)
                                        .build()
                        );
                    } else if (result == 2) {
                        requester.sendMessage(
                                Text.builder(
                                        "This player can't recieve teleport requests")
                                        .color(TextColors.RED)
                                        .build()
                        );
                    }
                } else {
                    requester.sendMessage(
                            Text.builder(
                                    "You must specify an online player to teleport to!")
                                    .color(TextColors.RED)
                                    .build()
                    );
                }
            }
        }
        return CommandResult.success();
    }
}
