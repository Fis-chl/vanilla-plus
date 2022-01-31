package me.fis_chl.vanillaplus.Warp.Command;

import me.fis_chl.vanillaplus.Warp.WarpHandler;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class SetWarpCommand implements CommandExecutor {

    private final WarpHandler warpHandler;

    public SetWarpCommand(WarpHandler warpHandler) {
        this.warpHandler = warpHandler;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof Player) {
            Player player = (Player) src;
            String warpName = (String) args.getOne("warpName").orElse(null);
            if (warpName != null) {
                boolean result = warpHandler.createWarp(player.getLocation(), warpName);
                if (result) {
                    player.sendMessage(
                            Text.join(
                                    Text.builder(
                                            "Warp '"
                                    ).color(TextColors.GREEN).build(),
                                    Text.builder(
                                            warpName
                                    ).color(TextColors.LIGHT_PURPLE).build(),
                                    Text.builder(
                                            "' created successfully!"
                                    ).color(TextColors.GREEN).build()
                            )
                    );
                } else {
                    player.sendMessage(
                            Text.builder(
                                    "A warp with that name already exists!"
                            ).color(TextColors.RED).build()
                    );
                }
            }
        }
        return CommandResult.success();
    }
}
