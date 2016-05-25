package com.fyxridd.lib.msg.config;

import com.fyxridd.lib.core.api.config.basic.Path;

@Path("msg")
public class MsgConfig {
    @Path("prefix.per")
    private String prefixPer;
    @Path("prefix.auto")
    private boolean prefixAuto;

    @Path("suffix.per")
    private String suffixPer;
    @Path("suffix.auto")
    private boolean suffixAuto;
    
    public String getPrefixPer() {
        return prefixPer;
    }

    public boolean isPrefixAuto() {
        return prefixAuto;
    }

    public String getSuffixPer() {
        return suffixPer;
    }

    public boolean isSuffixAuto() {
        return suffixAuto;
    }
}
