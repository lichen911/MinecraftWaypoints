package io.github.lichen911.waypoints;

import org.bukkit.Location;

import io.github.lichen911.waypoints.enums.WaypointType;

public class Waypoint {
    private final String playerName;
    private final String playerUuid;
    private final String name;
    private final WaypointType type;
    private final Location location;

    public Waypoint(String playerName, String playerUuid, String name, WaypointType type, Location location) {
        this.playerName = playerName;
        this.playerUuid = playerUuid;
        this.name = name;
        this.type = type;
        this.location = location;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public String getPlayerUuid() {
        return this.playerUuid;
    }

    public String getName() {
        return this.name;
    }

    public WaypointType getWaypointType() {
        return this.type;
    }

    public Location getLocation() {
        return this.location;
    }

    public String getCoordString() {
        return this.location.getBlockX() + " " + this.location.getBlockY() + " " + this.location.getBlockZ();
    }

    public String getBiome() {
        return this.location.getBlock().getBiome().toString();
    }
}
