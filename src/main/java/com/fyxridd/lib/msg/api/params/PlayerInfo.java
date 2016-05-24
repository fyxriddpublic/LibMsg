package com.fyxridd.lib.msg.api.params;

public class PlayerInfo {
    private String prefixType;
    private String suffixType;
    
    private String prefix;
    private String suffix;
    public PlayerInfo(String prefixType, String suffixType, String prefix, String suffix) {
        super();
        this.prefixType = prefixType;
        this.suffixType = suffixType;
        this.prefix = prefix;
        this.suffix = suffix;
    }
    public String getPrefixType() {
        return prefixType;
    }
    public String getSuffixType() {
        return suffixType;
    }
    public String getPrefix() {
        return prefix;
    }
    public String getSuffix() {
        return suffix;
    }
}
