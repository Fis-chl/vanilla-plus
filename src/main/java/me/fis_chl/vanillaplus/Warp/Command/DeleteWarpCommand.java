package me.fis_chl.vanillaplus.Warp.Command;

import me.fis_chl.vanillaplus.Warp.WarpHandler;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class DeleteWarpCommand implements CommandExecutor {

    private final WarpHandler warpHandler;

    public DeleteWarpCommand(WarpHandler warpHandler) {
        this.warpHandler = warpHandler;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String warpName = (String) args.getOne("warpName").orElse(null);
        if (warpName != null) {
            boolean result = warpHandler.deleteWarp(warpName);
            if (result) {
                src.sendMessage(
                        Text.join(
                                Text.builder(
                                        "Deleted warp '"
                                ).color(TextColors.GREEN).build(),
                                Text.builder(
                                        warpName
                                ).color(TextColors.LIGHT_PURPLE).build(),
                                Text.builder(
                                        "' successfully!"
                                ).color(TextColors.GREEN).build()
                        )
                );
            } else {
                src.sendMessage(
                        Text.builder(
                                "No warp with that name exists!"
                        ).color(TextColors.RED).build()
                );
            }
        }
        return CommandResult.success();
    }
}
