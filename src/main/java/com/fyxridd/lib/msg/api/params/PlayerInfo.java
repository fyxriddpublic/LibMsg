package com.fyxridd.lib.msg.api.params;

public class PlayerInfo {
    /**
     * @return 不为null可为""
     */
    private String prefixType;
    /**
     * @return 不为null可为""
     */
    private String suffixType;

    /**
     * @return 不为null可为""
     */
    private String prefix;
    /**
     * @return 不为null可为""
     */
    private String suffix;
    public PlayerInfo(String prefixType, String suffixType, String prefix, String suffix) {
        super();
        this.prefixType = prefixType != null?prefixType:"";
        this.suffixType = suffixType != null?suffixType:"";
        this.prefix = prefix != null?prefix:"";
        this.suffix = suffix != null?suffix:"";
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
