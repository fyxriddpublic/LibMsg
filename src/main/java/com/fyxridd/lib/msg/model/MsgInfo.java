package com.fyxridd.lib.msg.model;

/**
 * 前后缀信息
 */
public class MsgInfo {
    private String prefix;
    private String suffix;
    public MsgInfo(String prefix, String suffix) {
        super();
        this.prefix = prefix;
        this.suffix = suffix;
    }
    public String getPrefix() {
        return prefix;
    }
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    public String getSuffix() {
        return suffix;
    }
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}
