package io.github.lichen911.waypoints.managers;

import org.bukkit.entity.Player;

import io.github.lichen911.waypoints.enums.WaypointType;

public class PermissionManager {
    private final String permissionPathPrefix = "waypoints";

    public String getPermissionPath(String cmd, WaypointType wpType) {
        String subCmd = cmd.toString().toLowerCase();

        String permPath;
        if (wpType != null) {
            permPath = permissionPathPrefix + "." + wpType.text + "." + subCmd;
        } else {
            permPath = permissionPathPrefix + "." + subCmd;
        }
        return permPath;
    }

    public boolean checkHasPermission(Player player, String cmd, WaypointType wpType) {
        if (player.hasPermission(this.getPermissionPath(cmd, wpType))) {
            return true;
        }
        return false;
    }
}
