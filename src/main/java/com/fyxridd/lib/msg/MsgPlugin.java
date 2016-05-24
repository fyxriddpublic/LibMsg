package com.fyxridd.lib.msg;

import com.fyxridd.lib.core.api.plugin.SimplePlugin;

public class MsgPlugin extends SimplePlugin{
    public static MsgPlugin instance;

    @Override
    public void onEnable() {
        instance = this;

        super.onEnable();
    }
}