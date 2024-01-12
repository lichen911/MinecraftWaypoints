package io.github.lichen911.waypoints;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public final class Waypoints extends JavaPlugin {
    @Override
    public void onEnable() {
        Metrics metrics = new Metrics(this, 20727);

        getCommand(CommandLiteral.WP).setExecutor(new WpCommand(this));
    }
}