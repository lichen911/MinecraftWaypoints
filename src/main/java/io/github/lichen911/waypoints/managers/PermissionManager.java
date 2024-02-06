package io.github.lichen911.waypoints.managers;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import io.github.lichen911.waypoints.enums.WaypointType;

public class PermissionManager {
    private final String permissionPathPrefix = "waypoints";
    private final String adminPermission = "admin";
    private final String limitPermission = "limit";

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
        if (player.hasPermission(this.getPermissionPath(cmd, wpType)) || player.isOp()) {
            return true;
        }
        return false;
    }

    public boolean isAdmin(Player player) {
        if (player.hasPermission(permissionPathPrefix + "." + adminPermission) || player.isOp()) {
            return true;
        }
        return false;
    }

    public int getWaypointLimit(Player player, WaypointType wpType, int defaultValue) {
        String permPath = permissionPathPrefix + "." + wpType.text + "." + limitPermission + ".";

        for (PermissionAttachmentInfo attachmentInfo : player.getEffectivePermissions()) {
            String permission = attachmentInfo.getPermission();

            // With the way we iterate over effect permissions and return at the first
            // matching instance, we will have unexpected results if multiple permissions
            // define a limit.
            if (permission.startsWith(permPath) && attachmentInfo.getValue()) {
                return Integer.parseInt(permission.substring(permission.lastIndexOf(".") + 1));
            }
        }

        return defaultValue;
    }
}
