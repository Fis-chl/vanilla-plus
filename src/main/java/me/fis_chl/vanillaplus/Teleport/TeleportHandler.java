package me.fis_chl.vanillaplus.Teleport;

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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class TeleportHandler {

    private final Path configDir;
    private final Logger logger;
    private final List<TeleportRequest> currentRequests;
    private final HashMap<UUID, Boolean> tpPreferences;

    public TeleportHandler(Path configDir, Logger logger) {
        this.configDir = configDir;
        this.logger = logger;
        this.currentRequests = new ArrayList<>();
        this.tpPreferences = new HashMap<>();
        loadPlayerTpData();
        // Create a task to remove cancelled requests every 10 seconds
        PluginContainer plugin = Sponge.getPluginManager().getPlugin("vanillaplus").get();
        Task.Builder taskBuilder = Task.builder();
        taskBuilder.execute(
                () -> currentRequests.removeIf(TeleportRequest::isCancelled)
        ).interval(7L, TimeUnit.SECONDS).submit(plugin);
    }

    public int createRequest(Player pRequester, Player pDestination) {
        // Create a new teleport request with the specified requester and destination
        UUID requester = pRequester.getUniqueId();
        UUID destination = pDestination.getUniqueId();
        TeleportRequest cRequest = getRequest(requester);
        if (!tpPreferences.get(destination)) {
            return 2;
        }
        if (cRequest != null && cRequest.getRequester().equals(requester) && cRequest.getDestination().equals(destination)) {
            return 0;
        }
        cancellAllExistingRequests(requester);
        if (getRequest(requester) == null) {
            TeleportRequest newRequest = new TeleportRequest(requester, destination);
            currentRequests.add(newRequest);
            pRequester.sendMessage(
                    Text.builder(
                                    "Teleport request sent!")
                            .color(TextColors.GREEN)
                            .build()
            );
            pDestination.sendMessage(
                    Text.builder(
                                    pRequester.getName() + " has requested to teleport to you. Use /tpaccept to accept, or /tpdeny to deny")
                            .color(TextColors.GREEN)
                            .build()
            );
            // Remove the request after 60 seconds
            PluginContainer plugin = Sponge.getPluginManager().getPlugin("vanillaplus").get();
            Task.Builder taskBuilder = Task.builder();
            Text timeoutText = Text.builder(
                            "Teleport request expired!")
                    .color(TextColors.AQUA)
                    .build();
            taskBuilder.execute(
                    () -> {
                        if (currentRequests.contains(newRequest)) { // Check request is still in the list
                            if (!newRequest.isCancelled() && !newRequest.isAccepted()) {
                                pRequester.sendMessage(timeoutText);
                            }
                            currentRequests.remove(newRequest);
                        }
                    }
            ).delay(60L, TimeUnit.SECONDS).submit(plugin);
            return 1;
        } else {
            return -1;
        }
    }

    public boolean acceptRequest(UUID destination) {
        // Accept the last teleport request sent to the user
        List<TeleportRequest> requests = getRequestsToUser(destination);
        if (requests.size() > 0) {
            // Accept the last request sent to the user and cancel all others
            TeleportRequest acceptedRequest = requests.get(requests.size() - 1);
            Player pRequester = Sponge.getServer().getPlayer(acceptedRequest.getRequester()).orElse(null);
            Player pDestination = Sponge.getServer().getPlayer(acceptedRequest.getDestination()).orElse(null);
            cancelAllPendingRequests(destination);
            if (pRequester != null && pDestination != null) {
                // Inform and teleport both players
                pRequester.sendMessage(
                        Text.builder(
                                        pDestination.getName() + " accepted your teleport request, teleporting in 3 seconds...")
                                .color(TextColors.GREEN)
                                .build()
                );
                pDestination.sendMessage(
                        Text.builder(
                                        pRequester.getName() + " teleporting to your location in 3 seconds...")
                                .color(TextColors.GREEN)
                                .build()
                );
                PluginContainer plugin = Sponge.getPluginManager().getPlugin("vanillaplus").get();
                Task.Builder taskBuilder = Task.builder();
                taskBuilder.execute(
                        () -> {
                            pRequester.setLocation(pDestination.getLocation());
                        }
                ).delay(3, TimeUnit.SECONDS).submit(plugin);
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean denyRequest(UUID destination) {
        List<TeleportRequest> requests = getRequestsToUser(destination);
        Player pDestination = Sponge.getServer().getPlayer(destination).orElse(null);
        // Deny all requests
        if (requests.size() < 1) {
            return false;
        }
        for (TeleportRequest request : requests) {
            Player pRequester = Sponge.getServer().getPlayer(request.getRequester()).orElse(null);
            request.cancel();
            if (pRequester != null) {
                pRequester.sendMessage(
                        Text.builder(
                                pDestination.getName() + " denied your teleport request")
                                .color(TextColors.RED)
                                .build()
                );
            }
        }
        return true;
    }

    private void cancelAllPendingRequests(UUID destination) {
        // Cancel all the previous requests sent to this user
        for (TeleportRequest request : currentRequests) {
            if (request.getDestination().equals(destination)) {
                request.cancel();
            }
        }
    }

    private void cancellAllExistingRequests(UUID requester) {
        // Cancel all the existing requests the user has made
        for (TeleportRequest request : currentRequests) {
            if (request.getRequester().equals(requester)) {
                request.cancel();
            }
        }
    }

    private List<TeleportRequest> getRequestsToUser(UUID destination) {
        // Get all teleport requests that have the specified user as their destination
        List<TeleportRequest> requests = new ArrayList<>();
        for (TeleportRequest request : currentRequests) {
            if (request.getDestination().equals(destination) && !request.isCancelled()) {
                requests.add(request);
            }
        }
        return requests;
    }

    public boolean cancelRequest(UUID requester) {
        // Cancel a teleport request from the sender if it isn't already cancelled
        TeleportRequest request = getRequest(requester);
        if (request != null) {
            request.cancel();
            return true;
        } else {
            return false;
        }
    }

    private TeleportRequest getRequest(UUID requester) {
        // Check if the player has a currently pending teleport request
        // Return that request if they do, else return null
        for (TeleportRequest request : currentRequests) {
            if (request.getRequester().equals(requester)) {
                if (!request.isCancelled()) {
                    // Return the request if it isn't cancelled, otherwise just continue
                    return request;
                }
            }
        }
        return null;
    }

    public int changePlayerTpData(Player player) {
        // Toggles the player's preference in the hashmap
        UUID playerId = player.getUniqueId();
        boolean initVal = tpPreferences.get(playerId);
        if (tpPreferences.containsKey(playerId)) {
            tpPreferences.replace(playerId, (!tpPreferences.get(playerId)));
            updatePlayerTpData(player);
        } else {
            createPlayerTpData(player);
        }
        boolean newVal = tpPreferences.get(playerId);
        if (initVal == newVal) {  // If no change
            return -1;
        } else if (newVal) {
            return 1;
        } else {
            return 0;
        }
    }

    public void createPlayerTpData(Player player) {
        UUID playerId = player.getUniqueId();
        if (!tpPreferences.containsKey(playerId)) {
            tpPreferences.put(playerId, true);
            updatePlayerTpData(player);
        }
    }

    private void loadPlayerTpData() {
        // Load all player tp preferences into the dictionary
        Path tpPath = Paths.get(configDir.toString(), "teleportdata.hocon");
        ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder()
                .setPath(tpPath)
                .build();
        // Create an empty root node
        ConfigurationNode rootNode = loader.createEmptyNode(ConfigurationOptions.defaults());
        try {
            // Load the root node
            logger.info("Attempting to load 'teleportdata.hocon'");
            // Load subsequent data, if there is any
            logger.info(rootNode.toString());
            for (Object nodeName : rootNode.getChildrenMap().keySet()) {
                ConfigurationNode node = rootNode.getNode(nodeName);
                @SuppressWarnings("UnstableApiUsage") UUID playerId = node.getNode("uuid").getValue(TypeToken.of(UUID.class));
                boolean value = node.getNode("allowTpRequests").getBoolean();
                tpPreferences.put(playerId, value);
            }
            loader.save(rootNode);
            logger.info("Loaded 'teleportdata.hocon' successfully");
        } catch (IOException | ObjectMappingException e) {
            logger.error("Error loading 'teleportdata.hocon'");
        }
    }

    private void updatePlayerTpData(Player player) {
        // Update and save the config file if a player changes their toggle
        Path tpPath = Paths.get(configDir.toString(), "teleportdata.hocon");
        ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder()
                .setPath(tpPath)
                .build();
        // Create an empty root node
        ConfigurationNode rootNode = loader.createEmptyNode(ConfigurationOptions.defaults());
        try {
            // Load the root node
            rootNode = loader.load();
            // Load subsequent data, if there is any
            //noinspection UnstableApiUsage
            rootNode.getNode(player.getName(), "uuid").setValue(TypeToken.of(UUID.class), player.getUniqueId());
            rootNode.getNode(player.getName(), "allowTpRequests").setValue(tpPreferences.get(player.getUniqueId()));
            loader.save(rootNode);
        } catch (IOException | ObjectMappingException e) {
            logger.error("Error loading 'teleportdata.hocon'");
        }
    }
}
