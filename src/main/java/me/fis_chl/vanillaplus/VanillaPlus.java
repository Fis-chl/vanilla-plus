package me.fis_chl.vanillaplus;

import com.google.inject.Inject;
import me.fis_chl.vanillaplus.Command.*;
import me.fis_chl.vanillaplus.Listener.PlayerJoinListener;
import me.fis_chl.vanillaplus.Teleport.TeleportHandler;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import java.io.File;
import java.nio.file.Path;

@Plugin(
        id = "vanillaplus",
        name = "VanillaPlus",
        description = "Extra commands for use with Sponge 7.2.0 (mc version 1.12.2)",
        authors = {
                "Fis-chl"
        }
)
public class VanillaPlus {

    @Inject
    private Logger logger;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    private TeleportHandler teleportHandler;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        // Create config file directory if it doesn't exist
        File cfg = configDir.toFile();
        if (!cfg.exists()) {
            boolean result = cfg.mkdirs();
            if (result) {
                logger.info("Created config directory");
            } else {
                logger.error("Unable to create config directory");
            }
        }
        teleportHandler = new TeleportHandler(configDir, logger);
        registerCommands();
        registerListeners();
        logger.info("Loaded successfully!");
    }

    private void registerCommands() {
        // tpa command
        CommandSpec tpaSpec = CommandSpec.builder()
                .description(Text.of("Request to teleport to the specified player"))
                .permission("vp.tpa")
                .arguments(
                        GenericArguments.onlyOne(GenericArguments.player(Text.of("destinationPlayer")))
                )
                .executor(new TpaCommand(teleportHandler))
                .build();
        registerCommand(tpaSpec, "tpa", "tpr");
        // tpaccept command
        CommandSpec tpacceptSpec = CommandSpec.builder()
                .description(Text.of("Accept the latest pending teleport request"))
                .permission("vp.tpaccept")
                .executor(new TpacceptCommand(teleportHandler))
                .build();
        registerCommand(tpacceptSpec, "tpaccept");
        // tpdeny command
        CommandSpec tpdenySpec = CommandSpec.builder()
                .description(Text.of("Deny all currently pending teleport requests"))
                .permission("vp.tpdeny")
                .executor(new TpdenyCommand(teleportHandler))
                .build();
        registerCommand(tpdenySpec, "tpdeny", "tpd");
        // tpcancel command
        CommandSpec tpcancelSpec = CommandSpec.builder()
                .description(Text.of("Cancel your currently pending teleport request"))
                .permission("vp.tpcancel")
                .executor(new TpcancelCommand(teleportHandler))
                .build();
        registerCommand(tpcancelSpec, "tpcancel", "tpc");
        // tptoggle command
        CommandSpec tptoggleSpec = CommandSpec.builder()
                .description(Text.of("Toggles your ability to recieve teleport requests"))
                .permission("vp.tptoggle")
                .executor(new TptoggleCommand(teleportHandler))
                .build();
        registerCommand(tptoggleSpec, "tptoggle", "tpt");
    }

    private void registerCommand(CommandSpec toRegister, String... alias) {
        Sponge.getCommandManager().register(this, toRegister, alias);
    }

    private void registerListeners() {
        // join listener
        Sponge.getEventManager().registerListeners(this, new PlayerJoinListener(teleportHandler));
    }
}
