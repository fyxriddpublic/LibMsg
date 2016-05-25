package com.fyxridd.lib.msg.manager;

import com.fyxridd.lib.core.api.config.ConfigApi;
import com.fyxridd.lib.core.api.config.Setter;
import com.fyxridd.lib.func.api.FuncApi;
import com.fyxridd.lib.info.api.InfoApi;
import com.fyxridd.lib.msg.MsgPlugin;
import com.fyxridd.lib.msg.config.MsgConfig;
import com.fyxridd.lib.msg.func.MsgChat;
import com.fyxridd.lib.msg.func.MsgCmd;
import com.fyxridd.lib.msg.model.LevelData;
import com.fyxridd.lib.msg.model.LevelInfo;

import org.bukkit.Bukkit;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.EventExecutor;

import java.util.*;

/**
 * 前后缀的中间层
 */
public class MsgManager {
    //LibInfo的键
    private static final String PRE_SHOW_PREFIX = "PreShowPrefix";
    private static final String PRE_SHOW_SUFFIX = "PreShowSuffix";

    private MsgConfig config;

    //缓存

    //称号类型 称号信息
    private Map<String, LevelInfo> infos = new HashMap<>();

    //玩家名 称号类型 称号信息(内的level不为null)
    private Map<String, Map<String, LevelData>> prefixLevels = new HashMap<>(), suffixLevels = new HashMap<>();

    //玩家名 当前显示的称号类型,不为null
    private Map<String, String> nowPrefix = new HashMap<>(), nowSuffix = new HashMap<>();

    //允许自动选择的玩家列表
    private Set<String> enableAutos = new HashSet<>();

    public MsgManager() {
        //添加配置监听
        ConfigApi.addListener(MsgPlugin.instance.pn, MsgConfig.class, new Setter<MsgConfig>() {
            @Override
            public void set(MsgConfig value) {
                config = value;
            }
        });
        //注册功能
        FuncApi.register(MsgPlugin.instance.pn, new MsgCmd());
        FuncApi.register(MsgPlugin.instance.pn, new MsgChat());

        //注册事件
        {
            //玩家加入
            Bukkit.getPluginManager().registerEvent(PlayerJoinEvent.class, MsgPlugin.instance, EventPriority.LOWEST, new EventExecutor() {
                @Override
                public void execute(Listener listener, Event e) throws EventException {
                    PlayerJoinEvent event = (PlayerJoinEvent) e;
                    if (!prefixLevels.containsKey(event.getPlayer().getName())) prefixLevels.put(event.getPlayer().getName(), new HashMap<String, LevelData>());
                    if (!suffixLevels.containsKey(event.getPlayer().getName())) suffixLevels.put(event.getPlayer().getName(), new HashMap<String, LevelData>());
                }
            }, MsgPlugin.instance);
            Bukkit.getPluginManager().registerEvent(PlayerJoinEvent.class, MsgPlugin.instance, EventPriority.MONITOR, new EventExecutor() {
                @Override
                public void execute(Listener listener, Event e) throws EventException {
                    PlayerJoinEvent event = (PlayerJoinEvent) e;
                    String name = event.getPlayer().getName();
                    enableAutos.add(name);
                    //前缀
                    {
                        Map<String, LevelData> datas = prefixLevels.get(name);
                        if (datas != null) {
                            //检测先前显示的
                            LevelData levelData = datas.get(InfoApi.getInfo(name, PRE_SHOW_PREFIX));
                            if (levelData != null && isPrefix(levelData.getType())) {
                                setLevel(name, levelData.getType(), levelData.getLevel(), true, true);
                            }else {
                                //删除当前显示
                                setLevel(name, null, null, true, true);
                                //检测自动选择一个
                                checkAutoSel(name, true);
                            }
                        }
                    }

                    //后缀
                    {
                        Map<String, LevelData> datas = suffixLevels.get(name);
                        if (datas != null) {
                            //检测先前显示的
                            LevelData levelData = datas.get(InfoApi.getInfo(name, PRE_SHOW_SUFFIX));
                            if (levelData != null && !isPrefix(levelData.getType())) {
                                setLevel(name, levelData.getType(), levelData.getLevel(), false, true);
                            }else {
                                //删除当前显示
                                setLevel(name, null, null, false, true);
                                //检测自动选择一个
                                checkAutoSel(name, false);
                            }
                        }
                    }
                }
            }, MsgPlugin.instance);
            //玩家退出
            Bukkit.getPluginManager().registerEvent(PlayerQuitEvent.class, MsgPlugin.instance, EventPriority.NORMAL, new EventExecutor() {
                @Override
                public void execute(Listener listener, Event e) throws EventException {
                    PlayerQuitEvent event = (PlayerQuitEvent) e;
                    enableAutos.remove(event.getPlayer().getName());
                }
            }, MsgPlugin.instance);
        }
    }

    /**
     * @see com.fyxridd.lib.msg.api.MsgApi#registerLevel(String, boolean)
     */
    public void registerLevel(String type, boolean prefix) {
        infos.put(type, new LevelInfo(type, prefix));
    }

    /**
     * @see com.fyxridd.lib.msg.api.MsgApi#getLevel(String, String)
     */
    public String getLevel(String name, String type) {
        Map<String, LevelData> datas = isPrefix(type)?prefixLevels.get(name):suffixLevels.get(name);
        if (datas != null) {
            LevelData levelData = datas.get(type);
            if (levelData != null) return levelData.getLevel();
        }
        return null;
    }

    /**
     * @see com.fyxridd.lib.msg.api.MsgApi#setLevel(String, String, String)
     */
    public void setLevel(String name, String type, String level) {
        boolean isPrefix = isPrefix(type);
        //初始化
        Map<String, LevelData> datas = isPrefix?prefixLevels.get(name):suffixLevels.get(name);
        if (datas == null) {
            datas = new HashMap<>();
            if (isPrefix) prefixLevels.put(name, datas);
            else suffixLevels.put(name, datas);
        }
        //与旧的相同
        LevelData levelData = datas.get(type);
        if (levelData != null && levelData.getLevel().equals(level)) return;
        //处理
        boolean enableAuto = enableAutos.contains(name);
        String nowType = getNowType(name, isPrefix);
        if (level != null && !level.isEmpty()) {//设置
            datas.put(type, new LevelData(type, level));
            if (type.equals(nowType)) {
                //更新当前显示
                if (enableAuto) setLevel(name, nowType, level, isPrefix, true);
            }else {
                //检测自动选择新的
                if (enableAuto) checkAutoSel(name, isPrefix);
            }
        }else {//删除
            datas.remove(type);
            if (type.equals(nowType)) {
                //删除当前显示
                if (enableAuto) setLevel(name, null, null, isPrefix, true);
                //检测自动选择新的
                if (enableAuto) checkAutoSel(name, isPrefix);
            }
        }
    }

    /**
     * @see com.fyxridd.lib.msg.api.MsgApi#getNowType(String, boolean)
     */
    public String getNowType(String name, boolean prefix) {
        if (prefix) return nowPrefix.get(name);
        return nowSuffix.get(name);
    }

    public Collection<LevelData> getPrefixs(String name) {
        return prefixLevels.get(name).values();
    }

    public Collection<LevelData> getSuffixs(String name) {
        return suffixLevels.get(name).values();
    }

    /**
     * 检测指定类型是否显示为前缀
     */
    public boolean isPrefix(String type) {
        LevelInfo levelInfo = infos.get(type);
        return levelInfo == null || levelInfo.isPrefix();
    }

    /**
     * 检测自动选择
     * @param name 玩家名
     * @param prefix 是否前缀
     */
    public void checkAutoSel(String name, boolean prefix) {
        if ((prefix && config.isPrefixAuto() && getNowType(name, true) == null) || (!prefix && config.isSuffixAuto() && getNowType(name, false) == null)) {
            Map<String, LevelData> datas = prefix?prefixLevels.get(name):suffixLevels.get(name);
            if (datas != null) {
                for (LevelData levelData:datas.values()) {
                    if (prefix == isPrefix(levelData.getType())) {
                        setLevel(name, levelData.getType(), levelData.getLevel(), prefix, true);
                        break;
                    }
                }
            }
        }
    }

    /**
     * @param type 可为null
     * @param level 可为null
     * @param changePre 是否改变先前显示的
     */
    public void setLevel(String name, String type, String level, boolean prefix, boolean changePre) {
        if (prefix) {
            nowPrefix.put(name, type);
            MsgPlugin.instance.getScoreboardManager().setPrefix(name, level);
            if (changePre) InfoApi.setInfo(name, PRE_SHOW_PREFIX, type);
        }else {
            nowSuffix.put(name, type);
            MsgPlugin.instance.getScoreboardManager().setSuffix(name, level);
            if (changePre) InfoApi.setInfo(name, PRE_SHOW_SUFFIX, type);
        }
    }
}
