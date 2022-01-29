package me.fis_chl.vanillaplus.Command;

import me.fis_chl.vanillaplus.Warp.Warp;
import me.fis_chl.vanillaplus.Warp.WarpHandler;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class WarpCommand implements CommandExecutor {

    private final WarpHandler warpHandler;

    public WarpCommand(WarpHandler warpHandler) {
        this.warpHandler = warpHandler;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof Player) {
            Player player = (Player) src;
            String warpName = (String) args.getOne("warpName").orElse(null);
            if (warpName != null) {
                Warp warp = warpHandler.getWarp(warpName);
                if (warp != null) {
                    warpHandler.teleportPlayer(player, warp);
                } else {
                    player.sendMessage(
                            Text.builder(
                                    "No warp with that name exists!"
                            ).color(TextColors.RED).build()
                    );
                }
            }
        }
        return CommandResult.success();
    }
}
