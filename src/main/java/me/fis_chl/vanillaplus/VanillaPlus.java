package me.fis_chl.vanillaplus;

import com.google.inject.Inject;
import me.fis_chl.vanillaplus.Listener.PlayerJoinListener;
import me.fis_chl.vanillaplus.Teleport.Command.*;
import me.fis_chl.vanillaplus.Teleport.TeleportHandler;
import me.fis_chl.vanillaplus.Vault.Command.VaultCommand;
import me.fis_chl.vanillaplus.Vault.Command.VaultUpgradeCommand;
import me.fis_chl.vanillaplus.Vault.Command.ViewVaultCommand;
import me.fis_chl.vanillaplus.Vault.VaultHandler;
import me.fis_chl.vanillaplus.Vault.Listener.VaultCloseListener;
import me.fis_chl.vanillaplus.Warp.Command.DeleteWarpCommand;
import me.fis_chl.vanillaplus.Warp.Command.ListWarpsCommand;
import me.fis_chl.vanillaplus.Warp.Command.SetWarpCommand;
import me.fis_chl.vanillaplus.Warp.Command.WarpCommand;
import me.fis_chl.vanillaplus.Warp.WarpHandler;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
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

    @Inject
    private PluginContainer pluginContainer;

    private TeleportHandler teleportHandler;
    private WarpHandler warpHandler;
    private VaultHandler vaultHandler;

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
        warpHandler = new WarpHandler(configDir, logger);
        vaultHandler = new VaultHandler(configDir, logger);
        registerCommands();
        registerListeners();
        logger.info("Loaded successfully!");
    }

    @Listener
    public void reload(GameReloadEvent event) {
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
        warpHandler = new WarpHandler(configDir, logger);
        vaultHandler = new VaultHandler(configDir, logger);
        registerCommands();
        registerListeners();
        logger.info("Loaded successfully!");
    }

    private void registerCommands() {
        // Teleport commands
        // tpa command
        CommandSpec tpaSpec = CommandSpec.builder()
                .description(Text.of("Request to teleport to the specified player"))
                .permission("vp.user.tpa")
                .arguments(
                        GenericArguments.onlyOne(GenericArguments.player(Text.of("destinationPlayer")))
                )
                .executor(new TpaCommand(teleportHandler))
                .build();
        registerCommand(tpaSpec, "tpa", "tpr");
        // tpaccept command
        CommandSpec tpacceptSpec = CommandSpec.builder()
                .description(Text.of("Accept the latest pending teleport request"))
                .permission("vp.user.tpaccept")
                .executor(new TpacceptCommand(teleportHandler))
                .build();
        registerCommand(tpacceptSpec, "tpaccept");
        // tpdeny command
        CommandSpec tpdenySpec = CommandSpec.builder()
                .description(Text.of("Deny all currently pending teleport requests"))
                .permission("vp.user.tpdeny")
                .executor(new TpdenyCommand(teleportHandler))
                .build();
        registerCommand(tpdenySpec, "tpdeny", "tpd");
        // tpcancel command
        CommandSpec tpcancelSpec = CommandSpec.builder()
                .description(Text.of("Cancel your currently pending teleport request"))
                .permission("vp.user.tpcancel")
                .executor(new TpcancelCommand(teleportHandler))
                .build();
        registerCommand(tpcancelSpec, "tpcancel", "tpc");
        // tptoggle command
        CommandSpec tptoggleSpec = CommandSpec.builder()
                .description(Text.of("Toggles your ability to recieve teleport requests"))
                .permission("vp.user.tptoggle")
                .executor(new TptoggleCommand(teleportHandler))
                .build();
        registerCommand(tptoggleSpec, "tptoggle", "tpt");

        // Warp commands
        // warp command
        CommandSpec warpSpec = CommandSpec.builder()
                .description(Text.of("Teleport to the specified warp"))
                .permission("vp.user.warp")
                .arguments(
                        GenericArguments.onlyOne(GenericArguments.string(Text.of("warpName")))
                ).executor(new WarpCommand(warpHandler))
                .build();
        registerCommand(warpSpec, "warp");
        // setwarp command
        CommandSpec setwarpSpec = CommandSpec.builder()
                .description(Text.of("Set a warp with the specified name"))
                .permission("vp.admin.setwarp")
                .arguments(
                        GenericArguments.onlyOne(GenericArguments.string(Text.of("warpName")))
                )
                .executor(new SetWarpCommand(warpHandler))
                .build();
        registerCommand(setwarpSpec, "setwarp", "swarp", "createwarp", "cwarp");
        // deletewarp command
        CommandSpec deletewarpSpec = CommandSpec.builder()
                .description(Text.of("Delete the warp with the specified name"))
                .permission("vp.mod.deletewarp")
                .arguments(
                        GenericArguments.onlyOne(GenericArguments.string(Text.of("warpName")))
                )
                .executor(new DeleteWarpCommand(warpHandler))
                .build();
        registerCommand(deletewarpSpec, "deletewarp", "delwarp");
        // listwarps command
        CommandSpec listwarpSpec = CommandSpec.builder()
                .description(Text.of("List all available warps"))
                .permission("vp.user.listwarps")
                .executor(new ListWarpsCommand(warpHandler))
                .build();
        registerCommand(listwarpSpec, "listwarps", "warps");

        // Vaults section
        // Vault upgrade command
        CommandSpec vaultUpgradeSpec = CommandSpec.builder()
                .description(Text.of("Opens the vault upgrade menu"))
                .permission("vp.user.vault.upgrade")
                .executor(new VaultUpgradeCommand(vaultHandler))
                .build();
        // Vault command
        CommandSpec vaultSpec = CommandSpec.builder()
                .description(Text.of("Opens the player vault"))
                .permission("vp.user.vault")
                .child(
                    vaultUpgradeSpec, "upgrade", "upg"
                )
                .executor(new VaultCommand(vaultHandler))
                .build();
        registerCommand(vaultSpec, "vault", "v");
        // viewvault command
        CommandSpec viewvaultSpec = CommandSpec.builder()
                .description(Text.of("View the specified player's vault"))
                .permission("vp.admin.viewvault")
                .arguments(
                        GenericArguments.onlyOne(GenericArguments.player(Text.of("player")))
                )
                .executor(new ViewVaultCommand(vaultHandler))
                .build();
        registerCommand(viewvaultSpec, "viewvault", "vvault", "vv");
    }

    private void registerCommand(CommandSpec toRegister, String... alias) {
        Sponge.getCommandManager().register(this, toRegister, alias);
    }

    private void registerListeners() {
        // join listener
        Sponge.getEventManager().registerListeners(this, new PlayerJoinListener(teleportHandler));
        Sponge.getEventManager().registerListener(this, InteractInventoryEvent.Close.class, new VaultCloseListener(vaultHandler, logger));
    }
}
