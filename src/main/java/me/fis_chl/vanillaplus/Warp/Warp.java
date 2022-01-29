package me.fis_chl.vanillaplus.Warp;

import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class Warp {

    private final Location<World> location;
    private final String name;

    /**
     * Constructor.
     * @param location the location to set the warp to
     * @param name the name of the warp
     */
    public Warp(Location<World> location, String name) {
        this.location = location;
        this.name = name;
    }

    public Location<World> getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }
}
