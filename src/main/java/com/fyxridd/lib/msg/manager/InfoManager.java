package com.fyxridd.lib.msg.manager;

import com.fyxridd.lib.core.api.UtilApi;
import com.fyxridd.lib.msg.MsgPlugin;
import com.fyxridd.lib.msg.api.MsgApi;
import com.fyxridd.lib.msg.api.SideGetter;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.EventExecutor;

/**
 * 侧边栏获取器: 信息获取器
 * 此信息是静态的,玩家加入游戏时设置一次,在游戏中不会改变
 */
public class InfoManager {
    private static final String HANDLER_NAME = "info";

    public InfoManager() {
        //注册获取器
        MsgApi.registerSideHandler(HANDLER_NAME, new SideGetter() {
            /**
             * data会转换颜色字符,可用变量:
             *   {name}: 玩家名
             *   {display}: 玩家显示名
             */
            @Override
            public String get(Player p, String data) {
                return UtilApi.convert(data)
                        .replace("{name}", p.getName())
                        .replace("{display}", p.getDisplayName());
            }
        });
        //注册事件
        Bukkit.getPluginManager().registerEvent(PlayerJoinEvent.class, MsgPlugin.instance, EventPriority.HIGHEST, new EventExecutor() {
            @Override
            public void execute(Listener listener, Event e) throws EventException {
                PlayerJoinEvent event = (PlayerJoinEvent) e;
                MsgApi.updateSideShow(event.getPlayer(), HANDLER_NAME);
            }
        }, MsgPlugin.instance);
    }
}
