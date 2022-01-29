package me.fis_chl.vanillaplus.Teleport;

import java.util.UUID;

public class TeleportRequest {

    private final UUID requester;
    private final UUID destination;
    private boolean cancelled = false;
    private boolean accepted = false;

    public TeleportRequest(UUID requester, UUID destination) {
        this.requester = requester;
        this.destination = destination;
    }

    public void cancel() {
        cancelled = true;
    }

    public void accept() {
        accepted = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public UUID getRequester() {
        return requester;
    }

    public UUID getDestination(){
        return destination;
    }
}
