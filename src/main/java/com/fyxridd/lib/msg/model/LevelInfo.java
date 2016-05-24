package com.fyxridd.lib.msg.model;

public class LevelInfo {
    private String type;
    private boolean prefix;

    public LevelInfo(String type, boolean prefix) {
        this.type = type;
        this.prefix = prefix;
    }

    public String getType() {
        return type;
    }

    public boolean isPrefix() {
        return prefix;
    }
}
