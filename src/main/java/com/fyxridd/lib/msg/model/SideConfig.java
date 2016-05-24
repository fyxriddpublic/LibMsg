package com.fyxridd.lib.msg.model;

public class SideConfig {
    private String name;
    private String data;

    public SideConfig(String name, String data) {
        this.name = name;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public String getData() {
        return data;
    }
}
