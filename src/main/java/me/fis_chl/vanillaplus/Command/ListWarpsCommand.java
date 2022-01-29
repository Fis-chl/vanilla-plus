package me.fis_chl.vanillaplus.Command;

import me.fis_chl.vanillaplus.Warp.Warp;
import me.fis_chl.vanillaplus.Warp.WarpHandler;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;

public class ListWarpsCommand implements CommandExecutor {

    private final WarpHandler warpHandler;

    public ListWarpsCommand(WarpHandler warpHandler) {
        this.warpHandler = warpHandler;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        List<Warp> warps = warpHandler.getWarps();
        Text initialText = Text.builder(
                "Available warps:\n"
        ).color(TextColors.GREEN).build();
        for (Warp warp : warps) {
            initialText = Text.join(initialText,
                            Text.builder(
                                    "- "
                            ).color(TextColors.GOLD).build(),
                            Text.builder(
                                    warp.getName() + "\n"
                            ).color(TextColors.GREEN).build()
            );
        }
        src.sendMessage(initialText);
        return CommandResult.success();
    }
}
