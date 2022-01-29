package me.fis_chl.vanillaplus.Listener;

import me.fis_chl.vanillaplus.Teleport.TeleportHandler;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class PlayerJoinListener {

    private final TeleportHandler teleportHandler;

    public PlayerJoinListener(TeleportHandler teleportHandler) {
        this.teleportHandler = teleportHandler;
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join connectionEvent) {
        teleportHandler.createPlayerTpData(connectionEvent.getTargetEntity());
    }
}
