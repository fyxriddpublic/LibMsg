package com.fyxridd.lib.msg.model;

/**
 * 称号数据
 */
public class LevelData {
    //称号类型
    private String type;
    //称号,不为null
    private String level;

    public LevelData(String type, String level) {
        this.type = type;
        this.level = level;
    }

    public String getType() {
        return type;
    }

    public String getLevel() {
        return level;
    }
}
