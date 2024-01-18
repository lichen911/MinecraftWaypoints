package io.github.lichen911.waypoints;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.lichen911.waypoints.commands.WpCommand;
import io.github.lichen911.waypoints.managers.WaypointManager;
import io.github.lichen911.waypoints.utils.ConfigReader;

public final class Waypoints extends JavaPlugin {
    private static ConfigReader wpConfig;
    private static WaypointManager wpManager;

    @Override
    public void onEnable() {
        Metrics metrics = new Metrics(this, 20727);

        this.saveDefaultConfig();

        wpConfig = new ConfigReader(this, "", "waypoints.yml");
        wpConfig.saveDefaultConfig();
        wpManager = new WaypointManager(wpConfig);

        getCommand(CommandLiteral.WP).setExecutor(new WpCommand(this, wpManager));
    }
}