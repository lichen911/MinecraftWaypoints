package io.github.lichen911.waypoints.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CommandLiteral {
    public static final String WP = "wp";
    public static final String ADD = "add";
    public static final String RM = "rm";
    public static final String SET = "set";
    public static final String TP = "tp";
    public static final String PUB = "pub";
    public static final String LIST = "list";
    public static final String LISTNAMES = "listnames";

    public static final List<String> topLevelCmds = new ArrayList<String>(
            Arrays.asList(ADD, RM, TP, SET, LIST, LISTNAMES));

}