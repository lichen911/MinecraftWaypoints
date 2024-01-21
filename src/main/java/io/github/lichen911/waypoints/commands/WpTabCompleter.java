package io.github.lichen911.waypoints.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import io.github.lichen911.waypoints.managers.WaypointManager;
import io.github.lichen911.waypoints.objects.Waypoint;
import io.github.lichen911.waypoints.utils.CommandLiteral;

public class WpTabCompleter implements TabCompleter {
    private final WaypointManager wpManager;

    public WpTabCompleter(WaypointManager wpManager) {
        this.wpManager = wpManager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return null;
        }
        Player player = (Player) sender;

        if (args.length == 1) {
            return CommandLiteral.topLevelCmds;
        } else if (args.length == 2 && !args[0].equals(CommandLiteral.ADD) && !args[0].equals(CommandLiteral.LIST)) {
            return this.getWaypointList(player);
        } else if (args.length == 3 && !args[0].equals(CommandLiteral.LIST)) {
            return new ArrayList<String>(Arrays.asList(CommandLiteral.PUB));
        }
        return null;
    }

    public List<String> getWaypointList(Player player) {
        String playerUuid = player.getUniqueId().toString();
        List<String> waypointList = new ArrayList<String>();
        List<Waypoint> publicWaypoints = this.wpManager.getPublicWaypoints();
        List<Waypoint> privateWaypoints = this.wpManager.getPrivateWaypoints(playerUuid);

        for (Waypoint wp : publicWaypoints) {
            waypointList.add(wp.getName());
        }
        for (Waypoint wp : privateWaypoints) {
            waypointList.add(wp.getName());
        }

        return waypointList;
    }
}
