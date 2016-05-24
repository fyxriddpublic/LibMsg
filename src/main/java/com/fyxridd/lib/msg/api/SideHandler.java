package com.fyxridd.lib.msg.api;

import org.bukkit.entity.Player;

/**
 * 侧边栏值获取器
 */
public interface SideHandler {
    /**
     * 获取值
     * @param p 玩家
     * @param data 数据,可为空不为null
     * @return 值
     */
    String get(Player p, String data);
}
