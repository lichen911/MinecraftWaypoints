package io.github.lichen911.waypoints.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import io.github.lichen911.waypoints.enums.WaypointType;
import io.github.lichen911.waypoints.objects.Waypoint;
import io.github.lichen911.waypoints.utils.ConfigReader;

public class WaypointManager {
    private final ConfigReader wpConfig;

    private final String configPublicPrefix = "waypoints.public";
    private final String configPlayersPrefix = "waypoints.players";
    private final String configLocPath = "loc";
    private final String configUserPath = "userName";
    private final String configPublicOwnerPath = "owner";

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

        this.wpConfig.getConfig().set(configPath + "." + configLocPath, location);
        this.wpConfig.getConfig().set(configPath + "." + configUserPath, playerName);

        // Store the owner's UUID for public waypoints
        if (wp.getWaypointType() == WaypointType.PUBLIC) {
            this.wpConfig.getConfig().set(configPath + "." + configPublicOwnerPath, wp.getPlayerUuid());
        }

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

        // In the case of a public waypoint the returned Waypoint object should have the
        // playerUuid of the owner of the waypoint
        if (wpType == WaypointType.PUBLIC) {
            playerUuid = this.wpConfig.getConfig().getString(configPath + "." + configPublicOwnerPath);
        }

        Waypoint waypoint = new Waypoint(playerName, playerUuid, wpName, wpType, location);
        return waypoint;
    }

    private List<Waypoint> getWaypointList(String configPrefix, WaypointType wpType) {
        ConfigurationSection waypointConfigSection = this.wpConfig.getConfig().getConfigurationSection(configPrefix);

        List<Waypoint> waypoints = new ArrayList<Waypoint>();
        if (waypointConfigSection != null) {
            Map<String, Object> waypointMap = waypointConfigSection.getValues(false);

            for (Map.Entry<String, Object> entry : waypointMap.entrySet()) {
                String wpName = entry.getKey();
                String wpLocPath = configPrefix + "." + wpName + "." + configLocPath;
                Location wpLoc = this.wpConfig.getConfig().getLocation(wpLocPath);
                Waypoint waypoint = new Waypoint(null, null, wpName, wpType, wpLoc);
                waypoints.add(waypoint);
            }
        }
        return waypoints;
    }

    public List<Waypoint> getPublicWaypoints() {
        return this.getWaypointList(configPublicPrefix, WaypointType.PUBLIC);
    }

    public List<Waypoint> getPrivateWaypoints(String playerUuid) {
        String configPlayerPath = configPlayersPrefix + "." + playerUuid;
        return this.getWaypointList(configPlayerPath, WaypointType.PRIVATE);
    }
}
