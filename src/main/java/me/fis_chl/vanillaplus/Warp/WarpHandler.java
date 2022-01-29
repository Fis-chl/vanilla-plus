package me.fis_chl.vanillaplus.Warp;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class WarpHandler {

    private final Path configDir;
    private final Logger logger;
    private final List<Warp> warps;

    /**
     * Constructor.
     * @param configDir the folder containing all config files for the plugin
     * @param logger logger to print out information to the console
     */
    public WarpHandler(Path configDir, Logger logger) {
        this.configDir = configDir;
        this.logger = logger;
        this.warps = new ArrayList<>();
        loadWarpData();
    }

    public boolean createWarp(Location<World> location, String name) {
        Warp warp = new Warp(location, name);
        if (getWarp(name) == null) {
            warps.add(warp);
            saveWarpData(warp, false);
            return true;
        } else {
            return false;
        }
    }

    public boolean deleteWarp(String name) {
        Warp toDelete = getWarp(name);
        if (toDelete != null) {
            warps.remove(toDelete);
            saveWarpData(toDelete, true);
            return true;
        }
        return false;
    }

    public List<Warp> getWarps() {
        return warps;
    }

    public Warp getWarp(String name) {
        for (Warp warp : warps) {
            if (warp.getName().equals(name)) {
                return warp;
            }
        }
        return null;
    }

    public void teleportPlayer(Player player, Warp warp) {
        PluginContainer plugin = Sponge.getPluginManager().getPlugin("vanillaplus").get();
        Task.Builder taskBuilder = Task.builder();
        player.sendMessage(
                Text.join(
                        Text.builder(
                            "Teleporting to warp '"
                        ).color(TextColors.GREEN).build(),
                        Text.builder(
                                warp.getName()
                        ).color(TextColors.LIGHT_PURPLE).build(),
                        Text.builder(
                                "' in 3 seconds..."
                        ).color(TextColors.GREEN).build()
                )
        );
        taskBuilder.execute(
                () -> {
                    player.setLocation(warp.getLocation());
                }
        ).delay(3, TimeUnit.SECONDS).submit(plugin);
    }

    /**
     * Saves all warps to 'warpdata.hocon'
     */
    @SuppressWarnings("UnstableApiUsage")
    private void saveWarpData(Warp warp, boolean remove) {
        // Update and save the config file
        Path tpPath = Paths.get(configDir.toString(), "warpdata.hocon");
        ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder()
                .setPath(tpPath)
                .build();
        // Create an empty root node
        ConfigurationNode rootNode = loader.createEmptyNode(ConfigurationOptions.defaults());
        try {
            // Load the root node
            rootNode = loader.load();
            if (remove) {
                rootNode.removeChild(warp.getName());
            } else {
                // Get node for warp
                ConfigurationNode warpNode = rootNode.getNode(warp.getName());
                /*
                 Warp needs 4 bits of data:
                 - x, y, z
                 - world id
                 */
                int[] xyz = {
                        warp.getLocation().getBlockX(),
                        warp.getLocation().getBlockY(),
                        warp.getLocation().getBlockZ()
                };
                UUID worldId = warp.getLocation().getExtent().getUniqueId();
                warpNode.getNode("x").setValue(xyz[0]);
                warpNode.getNode("y").setValue(xyz[1]);
                warpNode.getNode("z").setValue(xyz[2]);
                warpNode.getNode("worldId").setValue(TypeToken.of(UUID.class), worldId);
            }
            loader.save(rootNode);
        } catch (IOException | ObjectMappingException e) {
            logger.error("Error loading 'warpdata.hocon'");
        }
    }

    /**
     * Loads all warps from 'warpdata.hocon'
     */
    @SuppressWarnings("UnstableApiUsage")
    private void loadWarpData() {
        // Update and save the config file
        Path tpPath = Paths.get(configDir.toString(), "warpdata.hocon");
        ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder()
                .setPath(tpPath)
                .build();
        // Create an empty root node
        ConfigurationNode rootNode = loader.createEmptyNode(ConfigurationOptions.defaults());
        try {
            // Load the root node
            rootNode = loader.load();
            for (Object nodeName : rootNode.getChildrenMap().keySet()) {
                ConfigurationNode node = rootNode.getNode(nodeName);
                                /*
                 Warp needs 4 bits of data:
                 - x, y, z
                 - world id
                 */
                int[] xyz = {
                        node.getNode("x").getInt(),
                        node.getNode("y").getInt(),
                        node.getNode("z").getInt()
                };
                UUID worldId = node.getNode("worldId").getValue(TypeToken.of(UUID.class));
                if (worldId != null) {
                    Sponge.getServer().loadWorld(worldId);
                    World world = Sponge.getServer().getWorld(worldId).orElse(null);
                    if (world != null) {
                        Location<World> warpLoc = world.getLocation(xyz[0], xyz[1], xyz[2]);
                        warps.add(new Warp(warpLoc, (String) nodeName));
                    }
                }
            }
            loader.save(rootNode);
            logger.info("Loaded 'warpdata.hocon' successfully");
        } catch (IOException | ObjectMappingException e) {
            logger.error("Error loading 'warpdata.hocon'");
        }
    }
}
