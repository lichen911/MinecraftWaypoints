package io.github.lichen911.waypoints.managers;

import org.bukkit.entity.Player;

import io.github.lichen911.waypoints.Waypoints;
import io.github.lichen911.waypoints.enums.WaypointType;
import io.github.lichen911.waypoints.utils.CommandLiteral;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ClickableChatManager {
    private final Waypoints plugin;
    private final PermissionManager permManager;
    private final String chatConfigPrefix = "clickableChat";
    private final String wpCmdPrefix = "/wp";

    public ClickableChatManager(Waypoints plugin, PermissionManager permManager) {
        this.plugin = plugin;
        this.permManager = permManager;
    }

    public boolean getClickableChatConfig(String setting) {
        return this.plugin.getConfig().getBoolean(chatConfigPrefix + "." + setting);
    }

    private String buildCommand(String cmd, String wpName, WaypointType wpType) {
        String fullCmd = wpCmdPrefix + " " + cmd + " " + wpName;

        if (wpType == WaypointType.PUBLIC) {
            fullCmd += " " + CommandLiteral.PUB;
        }

        return fullCmd;
    }

    public TextComponent getClickableCommands(Player player, String wpName, WaypointType wpType) {
        boolean useRunCmd = this.getClickableChatConfig("clicksRunCommands");
        ClickEvent.Action clickEventAction;
        if (useRunCmd) {
            clickEventAction = ClickEvent.Action.RUN_COMMAND;
        } else {
            clickEventAction = ClickEvent.Action.SUGGEST_COMMAND;
        }

        boolean hasSetPerm = this.permManager.checkHasPermission(player, CommandLiteral.SET, wpType);
        boolean hasTpPerm = this.permManager.checkHasPermission(player, CommandLiteral.TP, wpType);
        boolean hasRmPerm = this.permManager.checkHasPermission(player, CommandLiteral.RM, wpType);

        TextComponent msg = null;
        boolean priorCmd = false;

        if (hasSetPerm || hasTpPerm || hasRmPerm) {
            msg = new TextComponent("(");
            msg.setColor(ChatColor.WHITE);

            if (hasSetPerm) {
                TextComponent setOpt = new TextComponent("set");
                setOpt.setColor(ChatColor.GOLD);
                setOpt.setClickEvent(
                        new ClickEvent(clickEventAction, this.buildCommand(CommandLiteral.SET, wpName, wpType)));
                msg.addExtra(setOpt);
                priorCmd = true;
            }

            if (hasTpPerm) {
                if (priorCmd) {
                    msg.addExtra(" ");
                }
                TextComponent tpOpt = new TextComponent("tp");
                tpOpt.setColor(ChatColor.GOLD);
                tpOpt.setClickEvent(
                        new ClickEvent(clickEventAction, this.buildCommand(CommandLiteral.TP, wpName, wpType)));
                msg.addExtra(tpOpt);
                priorCmd = true;
            }

            if (hasRmPerm) {
                if (priorCmd) {
                    msg.addExtra(" ");
                }
                TextComponent rmOpt = new TextComponent("rm");
                rmOpt.setColor(ChatColor.GOLD);
                // Always suggest command for RM so we don't accidentally delete waypoints
                rmOpt.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                        this.buildCommand(CommandLiteral.RM, wpName, wpType)));
                msg.addExtra(rmOpt);
            }

            msg.addExtra(")");
            msg.setColor(ChatColor.WHITE);
        }

        return msg;
    }
}
