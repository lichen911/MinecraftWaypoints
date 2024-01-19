package io.github.lichen911.waypoints;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.lichen911.waypoints.commands.WpCommand;
import io.github.lichen911.waypoints.managers.ClickableChatManager;
import io.github.lichen911.waypoints.managers.PermissionManager;
import io.github.lichen911.waypoints.managers.WaypointManager;
import io.github.lichen911.waypoints.utils.CommandLiteral;
import io.github.lichen911.waypoints.utils.ConfigReader;

public final class Waypoints extends JavaPlugin {
    private static ConfigReader wpConfig;
    private static WaypointManager wpManager;
    private static PermissionManager permManager;
    private ClickableChatManager ccManager;

    private final String configResponseMsgPrefix = "responseMessages";

    @Override
    public void onEnable() {
        Metrics metrics = new Metrics(this, 20727);

        this.saveDefaultConfig();

        wpConfig = new ConfigReader(this, "", "waypoints.yml");
        wpConfig.saveDefaultConfig();

        wpManager = new WaypointManager(wpConfig);
        permManager = new PermissionManager();
        ccManager = new ClickableChatManager(this, permManager);

        getCommand(CommandLiteral.WP).setExecutor(new WpCommand(this, wpManager, permManager, ccManager));
    }

    public String getResponseMessage(String msgName) {
        return this.getConfig().getString(configResponseMsgPrefix + "." + msgName);
    }

}