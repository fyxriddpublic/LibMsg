package com.fyxridd.lib.msg.manager;

import com.fyxridd.lib.msg.MsgPlugin;
import com.fyxridd.lib.msg.api.MsgApi;
import com.fyxridd.lib.msg.api.params.PlayerInfo;
import com.fyxridd.lib.params.api.Getter;
import com.fyxridd.lib.params.api.ParamsApi;

/**
 * 暴露给LibParams的变量内容
 */
public class ParamsManager {
    public static final String GET_PLAYER_INFO = "getPlayerInfo";
    
    public ParamsManager() {
        //注册变量
        ParamsApi.register(MsgPlugin.instance.pn, GET_PLAYER_INFO, new Getter() {
            /**
             * @param args '玩家名'
             */
            @Override
            public Object get(String... args) {
                String player = args[0];
                String prefixType = MsgPlugin.instance.getMsgManager().getNowType(player, true);
                String suffixType = MsgPlugin.instance.getMsgManager().getNowType(player, false);
                String prefix = MsgApi.getPrefix(player);
                String suffix = MsgApi.getSuffix(player);
                return new PlayerInfo(prefixType, suffixType, prefix, suffix);
            }
        });
    }
}
