package io.github.lichen911.waypoints.managers;

import org.bukkit.Location;

import io.github.lichen911.waypoints.Waypoint;
import io.github.lichen911.waypoints.enums.WaypointType;
import io.github.lichen911.waypoints.utils.ConfigReader;

public class WaypointManager {
    private final ConfigReader wpConfig;

    private final String configPublicPrefix = "waypoints.public";
    private final String configPlayersPrefix = "waypoints.players";
    private final String configLocPath = "loc";
    private final String configBiomePath = "biome";
    private final String configUserPath = "userName";
    private final String lastWpName = "last";
    private final String wpTypeNamePub = "public";
    private final String wpTypeNamePriv = "private";
    private final String chatConfigPrefix = "clickableChat";

    public WaypointManager(ConfigReader wpConfig) {
        this.wpConfig = wpConfig;
    }

    public String getWpConfigPath(String playerUuid, String wpName, WaypointType wpType) {
        String configPath;
        if (wpType == WaypointType.PUBLIC) {
            configPath = configPublicPrefix + "." + wpName;
        } else {
            configPath = configPlayersPrefix + "." + playerUuid + "." + wpName;
        }

        return configPath;
    }

    public Location getLocationFromConfig(String playerUuid, String wpName, WaypointType wpType) {
        String configPath = this.getWpConfigPath(playerUuid, wpName, wpType) + "." + configLocPath;
        Location location = this.wpConfig.getConfig().getLocation(configPath);
        return location;
    }

    public void addWaypoint(Waypoint wp) {
        String configPath = this.getWpConfigPath(wp.getPlayerUuid(), wp.getName(), wp.getWaypointType());
        String playerName = wp.getPlayerName();
        Location location = wp.getLocation();
        String playerBiome = location.getBlock().getBiome().toString();

        this.wpConfig.getConfig().set(configPath + "." + configLocPath, location);
        this.wpConfig.getConfig().set(configPath + "." + configBiomePath, playerBiome);
        this.wpConfig.getConfig().set(configPath + "." + configUserPath, playerName);
        this.wpConfig.saveConfig();
    }

    public void rmWaypoint(Waypoint wp) {
        String configPath = this.getWpConfigPath(wp.getPlayerUuid(), wp.getName(), wp.getWaypointType());

        this.wpConfig.getConfig().set(configPath, null);
        this.wpConfig.saveConfig();

    }

    public Waypoint getWaypoint(String playerUuid, String wpName, WaypointType wpType) {
        String configPath = this.getWpConfigPath(playerUuid, wpName, wpType);
        String playerName = this.wpConfig.getConfig().getString(configPath + "." + configUserPath);
        Location location = this.getLocationFromConfig(playerUuid, wpName, wpType);

        Waypoint waypoint = new Waypoint(playerName, playerUuid, wpName, wpType, location);
        return waypoint;

    }
}
