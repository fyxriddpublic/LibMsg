package com.fyxridd.lib.msg.manager;

import com.fyxridd.lib.core.api.config.ConfigApi;
import com.fyxridd.lib.core.api.config.Setter;
import com.fyxridd.lib.func.api.FuncApi;
import com.fyxridd.lib.info.api.InfoApi;
import com.fyxridd.lib.msg.MsgPlugin;
import com.fyxridd.lib.msg.config.MsgConfig;
import com.fyxridd.lib.msg.func.MsgChat;
import com.fyxridd.lib.msg.model.LevelData;
import com.fyxridd.lib.msg.model.LevelInfo;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

/**
 * 前后缀的中间层
 */
public class MsgManager implements Listener {
    //LibInfo的键
    private static final String PRE_SHOW_PREFIX = "PreShowPrefix";
    private static final String PRE_SHOW_SUFFIX = "PreShowSuffix";

    private MsgConfig config;

    //缓存

    //称号类型 称号信息
    private HashMap<String, LevelInfo> infos = new HashMap<>();

    //玩家名 称号类型 称号信息(内的level不为null)
    private HashMap<String, HashMap<String, LevelData>> prefixLevels = new HashMap<>(), suffixLevels = new HashMap<>();

    //玩家名 当前显示的称号类型,不为null
    private HashMap<String, String> nowPrefix = new HashMap<>(), nowSuffix = new HashMap<>();

    //允许自动选择的玩家列表
    private HashSet<String> enableAutos = new HashSet<>();

    public MsgManager() {
        //添加配置监听
        ConfigApi.addListener(MsgPlugin.instance.pn, MsgConfig.class, new Setter<MsgConfig>() {
            @Override
            public void set(MsgConfig value) {
                config = value;
            }
        });
        //注册功能
        FuncApi.register(MsgPlugin.instance.pn, new MsgChat());
        
        Bukkit.getPluginManager().registerEvents(this, MsgPlugin.instance);
    }

    @EventHandler(priority= EventPriority.LOWEST)
    public void onPlayerJoinLowest(PlayerJoinEvent e) {
        if (!prefixLevels.containsKey(e.getPlayer().getName())) prefixLevels.put(e.getPlayer().getName(), new HashMap<String, LevelData>());
        if (!suffixLevels.containsKey(e.getPlayer().getName())) suffixLevels.put(e.getPlayer().getName(), new HashMap<String, LevelData>());
    }

    @EventHandler(priority= EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e) {
        String name = e.getPlayer().getName();
        enableAutos.add(name);
        //前缀
        {
            HashMap<String, LevelData> datas = prefixLevels.get(name);
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
            HashMap<String, LevelData> datas = suffixLevels.get(name);
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

    @EventHandler(priority= EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent e) {
        enableAutos.remove(e.getPlayer().getName());
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
        HashMap<String, LevelData> datas = isPrefix(type)?prefixLevels.get(name):suffixLevels.get(name);
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
        HashMap<String, LevelData> datas = isPrefix?prefixLevels.get(name):suffixLevels.get(name);
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

    public Collection<LevelInfo> getLevelInfos() {
        return infos.values();
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
            HashMap<String, LevelData> datas = prefix?prefixLevels.get(name):suffixLevels.get(name);
            if (datas != null) {
                for (LevelData levelData:datas.values()) {
                    if (!(prefix ^ isPrefix(levelData.getType()))) {
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
