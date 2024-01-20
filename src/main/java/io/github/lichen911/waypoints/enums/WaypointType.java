package io.github.lichen911.waypoints.enums;

public enum WaypointType {
    PUBLIC("public"), PRIVATE("private");

    public final String text;

    private WaypointType(String text) {
        this.text = text;
    }
}
