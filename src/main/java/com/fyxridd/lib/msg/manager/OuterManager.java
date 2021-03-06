package com.fyxridd.lib.msg.manager;

import com.fyxridd.lib.msg.MsgPlugin;
import com.fyxridd.lib.msg.api.MsgApi;
import com.fyxridd.lib.msg.model.LevelData;
import com.fyxridd.lib.params.api.Getter;
import com.fyxridd.lib.params.api.ParamsApi;
import com.fyxridd.lib.show.chat.api.ShowApi;
import com.fyxridd.lib.show.chat.api.show.ShowList;
import com.fyxridd.lib.show.chat.api.show.ShowListGetter;

/**
 * 对外暴露的内容
 */
public class OuterManager {
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

    
    public static final String GET_PLAYER_INFO = "getPlayerInfo";
    public static final String GET_ALL_PREFIX = "getPrefixes";
    public static final String GET_ALL_SUFFIX = "getSuffixes";
    
    public OuterManager() {
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

        //注册列表获取器
        ShowApi.register(MsgPlugin.instance.pn, GET_ALL_PREFIX, new ShowListGetter() {
            @Override
            public ShowList handle(String name, String arg) {
                return ShowApi.getShowList(2, MsgPlugin.instance.getMsgManager().getPrefixs(name), LevelData.class);
            }
        });
        ShowApi.register(MsgPlugin.instance.pn, GET_ALL_SUFFIX, new ShowListGetter() {
            @Override
            public ShowList handle(String name, String arg) {
                return ShowApi.getShowList(2, MsgPlugin.instance.getMsgManager().getSuffixs(name), LevelData.class);
            }
        });
    }
}
