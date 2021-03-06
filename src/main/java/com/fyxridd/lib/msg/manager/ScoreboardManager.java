package com.fyxridd.lib.msg.manager;

import com.comphenix.packetwrapper.WrapperPlayServerScoreboardDisplayObjective;
import com.comphenix.packetwrapper.WrapperPlayServerScoreboardObjective;
import com.comphenix.packetwrapper.WrapperPlayServerScoreboardScore;
import com.comphenix.packetwrapper.WrapperPlayServerScoreboardTeam;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.fyxridd.lib.core.api.PerApi;
import com.fyxridd.lib.core.api.PlayerApi;
import com.fyxridd.lib.core.api.config.ConfigApi;
import com.fyxridd.lib.core.api.config.Setter;
import com.fyxridd.lib.msg.MsgPlugin;
import com.fyxridd.lib.msg.api.SideGetter;
import com.fyxridd.lib.msg.config.ScoreboardConfig;
import com.fyxridd.lib.msg.model.MsgInfo;
import com.fyxridd.lib.msg.model.SideConfig;
import com.fyxridd.lib.msg.model.SideInfo;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.EventExecutor;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * 负责与底层包交互
 */
public class ScoreboardManager {
    private static final String colors = "abcdef0123456789";
    //没有这权限:显示侧边栏 有这权限:不显示侧边栏
    private static final String CHECK_PER = "lib.msg.checkShow";
    private static ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    private ScoreboardConfig scoreboardConfig;

    //缓存

    //玩家名,前后缀信息
    private static Map<String, MsgInfo> msgInfos = new HashMap<>();
    //玩家名,队伍的只包含自己的信息
    private static Map<String, List<String>> players = new HashMap<>();

    //玩家名,侧边栏信息
    private static Map<String, SideInfo> shows = new HashMap<>();
    //侧边栏获取器名 侧边栏获取器
    private static Map<String, SideGetter> sideGetters = new HashMap<>();

    public ScoreboardManager() {
        //添加配置监听
        ConfigApi.addListener(MsgPlugin.instance.pn, ScoreboardConfig.class, new Setter<ScoreboardConfig>() {
            @Override
            public void set(ScoreboardConfig value) {
                scoreboardConfig = value;
            }
        });
        //注册事件
        {
            //玩家加入
            Bukkit.getPluginManager().registerEvent(PlayerJoinEvent.class, MsgPlugin.instance, EventPriority.LOWEST, new EventExecutor() {
                @Override
                public void execute(Listener listener, Event e) throws EventException {
                    if (e instanceof PlayerJoinEvent) {
                        PlayerJoinEvent event = (PlayerJoinEvent) e;
                        Player p = event.getPlayer();
                        String name = p.getName();
                        if (!players.containsKey(name)) {
                            List<String> list = new ArrayList<String>();
                            list.add(name);
                            players.put(name, list);
                        }

                        MsgInfo msgInfo0 = getMsgInfo(p.getName());
                        List<String> list0 = players.get(name);
                        PacketContainer createTeamPacketSelf = getCreateTeamPacket(name, msgInfo0.getPrefix(), msgInfo0.getSuffix(), list0);;
                        for (Player tar:Bukkit.getOnlinePlayers()) {
                            if (!tar.equals(p)) send(tar, createTeamPacketSelf);

                            MsgInfo msgInfo = getMsgInfo(tar.getName());
                            PacketContainer createTeamPacketOther = getCreateTeamPacket(tar.getName(), msgInfo.getPrefix(), msgInfo.getSuffix(), players.get(tar.getName()));
                            send(p, createTeamPacketOther);
                        }
                        //侧边栏检测
                        if (isDisplaySideBar(p)) {
                            SideInfo sideInfo = getShowInfo(name);
                            List<PacketContainer> createSidePackets = getCreateSidePackets(name, sideInfo.getShow(), sideInfo.getFrom());
                            for (PacketContainer pc : createSidePackets) send(p, pc);
                        }
                    }
                }
            }, MsgPlugin.instance);
            //玩家退出
            Bukkit.getPluginManager().registerEvent(PlayerQuitEvent.class, MsgPlugin.instance, EventPriority.LOWEST, new EventExecutor() {
                @Override
                public void execute(Listener listener, Event e) throws EventException {
                    if (e instanceof PlayerQuitEvent) {
                        PlayerQuitEvent event = (PlayerQuitEvent) e;
                        Player p = event.getPlayer();
                        PacketContainer pc = getRemoveTeamPacket(p.getName());
                        for (Player tar:Bukkit.getOnlinePlayers()) {
                            if (!tar.getName().equals(p.getName())) send(tar, pc);
                        }
                    }
                }
            }, MsgPlugin.instance);
        }
    }

    /**
     * 设置玩家名字前缀(头上的名字)
     * @param name 玩家,不为null
     * @param prefix 前缀,最长16字符,超过会被截断,null或空表示取消前缀
     */
    public void setPrefix(String name, String prefix) {
        if (prefix == null) prefix = "";
        if (prefix.length() > 16) prefix = prefix.substring(0, 16);

        MsgInfo msgInfo = getMsgInfo(name);
        if (msgInfo.getPrefix() != null && msgInfo.getPrefix().equals(prefix)) return;//与原来一样
        msgInfo.setPrefix(prefix);
        PacketContainer pc = getUpdateTeamInfoPacket(name, msgInfo.getPrefix(), msgInfo.getSuffix());
        for (Player tar:Bukkit.getOnlinePlayers()) send(tar, pc);
    }

    /**
     * @see com.fyxridd.lib.msg.api.MsgApi#getPrefix(String)
     */
    public String getPrefix(String name) {
        if (name == null) return null;

        //玩家存在性检测
        name = PlayerApi.getRealName(null, name);
        if (name == null) return null;
        //
        return getMsgInfo(name).getPrefix();
    }

    /**
     * 设置玩家名字后缀(头上的名字)
     * @param name 玩家名,不为null
     * @param suffix 后缀,最长16字符,超过会被截断,null或空表示取消后缀
     */
    public void setSuffix(String name, String suffix) {
        if (suffix == null) suffix = "";
        if (suffix.length() > 16) suffix = suffix.substring(0, 16);

        MsgInfo msgInfo = getMsgInfo(name);
        if (msgInfo.getSuffix() != null && msgInfo.getSuffix().equals(suffix)) return;//与原来一样
        msgInfo.setSuffix(suffix);
        PacketContainer pc = getUpdateTeamInfoPacket(name, msgInfo.getPrefix(), msgInfo.getSuffix());
        for (Player tar:Bukkit.getOnlinePlayers()) send(tar, pc);
    }

    /**
     * @see com.fyxridd.lib.msg.api.MsgApi#getSuffix(String)
     */
    public String getSuffix(String name) {
        if (name == null) return null;

        //玩家存在性检测
        name = PlayerApi.getRealName(null, name);
        if (name == null) return null;
        //
        return getMsgInfo(name).getSuffix();
    }

    /**
     * @see com.fyxridd.lib.msg.api.MsgApi#setDisplaySideBar(org.bukkit.entity.Player, boolean)
     */
    public void setDisplaySideBar(Player p, boolean display) {
        if (p == null || !p.isOnline()) return;

        if (display) {
            if (!isDisplaySideBar(p)) {
                PerApi.del(p.getName(), CHECK_PER);
                SideInfo sideInfo = getShowInfo(p.getName());
                List<PacketContainer> list = getCreateSidePackets(p.getName(), sideInfo.getShow(), sideInfo.getFrom());
                for (PacketContainer pc:list) send(p, pc);
            }
        }else {
            if (isDisplaySideBar(p)) {
                PerApi.add(p.getName(), CHECK_PER);
                PacketContainer removeSidePacket = getRemoveSidePacket(p.getName());
                send(p, removeSidePacket);
            }
        }
    }

    /**
     * @see com.fyxridd.lib.msg.api.MsgApi#isDisplaySideBar(org.bukkit.entity.Player)
     */
    public boolean isDisplaySideBar(Player p) {
        if (p == null) return false;

        return !PerApi.has(p.getName(), CHECK_PER);
    }

    /**
     * 设置玩家的侧边栏显示标题
     * @param p 玩家,可为null(null或不在线时无效果)
     * @param title 显示标题,超过32字符会被截断,可为null
     */
    public void setSideShowTitle(Player p, String title) {
        if (p == null || !p.isOnline()) return;

        if (title == null) title = "";
        else if (title.length() > 32) title = title.substring(0, 32);

        //更新缓存
        SideInfo sideInfo = getShowInfo(p.getName());
        sideInfo.setShow(title);
        //检测发送包
        if (isDisplaySideBar(p)) {
            PacketContainer pc = getUpdateSideTitlePacket(p.getName(), title);
            send(p, pc);
        }
    }

    /**
     * 设置玩家的侧边栏显示行内容<br>
     * 注意:行内容不能一样,否则将只显示一个
     * @param p 玩家,不为null且在线
     * @param index 行位置,从下往上排,0-(sideSize-1)
     * @param show 行内容,超过14字符会被截断,可为null
     */
    public void setSideShowItem(Player p, int index, String show) {
        if (p == null || !p.isOnline()) return;

        if (index < 0 || index >= scoreboardConfig.getSideSize()) return;

        if (show == null) show = "";
        else if (show.length() > 14) show = show.substring(0, 14);

        SideInfo sideInfo = getShowInfo(p.getName());
        //检测发送包
        if (isDisplaySideBar(p)) {
            PacketContainer pc = getRemoveSideItemPacket(p.getName(), index, sideInfo.getFrom().get(index));
            send(p, pc);
            pc = getUpdateSideItemPacket(p.getName(), index, show);
            send(p, pc);
        }
        //更新缓存
        sideInfo.getFrom().set(index, show);
    }

    /**
     * @see com.fyxridd.lib.msg.api.MsgApi#registerSideGetter(String, SideGetter)
     */
    public void registerSideHandler(String name, SideGetter sideGetter) {
        sideGetters.put(name, sideGetter);
    }

    /**
     * @see com.fyxridd.lib.msg.api.MsgApi#updateSideShow(org.bukkit.entity.Player, String)
     */
    public void updateSideShow(Player p, String name) {
        SideGetter sideGetter = sideGetters.get(name);
        if (sideGetter != null) {
            for (Map.Entry<Integer, SideConfig> entry:scoreboardConfig.getSides().entrySet()) {
                if (entry.getValue().getName().equals(name)) {
                    String content = sideGetter.get(p, entry.getValue().getData());
                    if (entry.getKey() == -1) setSideShowTitle(p, content);
                    else setSideShowItem(p, entry.getKey(), content);
                }
            }
        }
    }

    /**
     * 获取创建队伍信息包
     * @param name 不为null
     * @param prefix 可为null
     * @param suffix 可为null
     * @param players 可为null
     */
    private PacketContainer getCreateTeamPacket(String name, String prefix, String suffix, List<String> players) {
        if (prefix == null) prefix = "";
        if (suffix == null) suffix = "";
        if (players == null) players = new ArrayList<>();

        WrapperPlayServerScoreboardTeam team = new WrapperPlayServerScoreboardTeam();
        team.setTeamName(name);
        team.setTeamPrefix(prefix);
        team.setTeamSuffix(suffix);
        team.setPlayers(players);
        team.setMode(0);
        return team.getHandle();
    }

    /**
     * 获取更新队伍信息包
     * @param name 不为null
     * @param prefix 可为null
     * @param suffix 可为null
     */
    private PacketContainer getUpdateTeamInfoPacket(String name, String prefix, String suffix) {
        if (prefix == null) prefix = "";
        if (suffix == null) suffix = "";

        WrapperPlayServerScoreboardTeam team = new WrapperPlayServerScoreboardTeam();
        team.setTeamName(name);
        team.setTeamPrefix(prefix);
        team.setTeamSuffix(suffix);
        team.setMode(2);
        return team.getHandle();
    }

    /**
     * 获取去除队伍信息包
     * @param name 不为null
     */
    private PacketContainer getRemoveTeamPacket(String name) {
        WrapperPlayServerScoreboardTeam team = new WrapperPlayServerScoreboardTeam();
        team.setTeamName(name);
        team.setMode(1);
        return team.getHandle();
    }

    /**
     * 获取显示侧边栏信息包
     * @param name 玩家名(用作objective的唯一标识),不为null
     * @param show 显示的信息(如果超过32字符会自动截取前32字符),可为null
     * @param froms 发出者s,可为null
     */
    private List<PacketContainer> getCreateSidePackets(String name, String show, List<String> froms) {
        List<PacketContainer> result = new ArrayList<>();
        if (show == null) show = "";
        show = show.substring(0, Math.min(32, show.length()));
        if (froms == null) froms = new ArrayList<>();

        //创建obj

        WrapperPlayServerScoreboardObjective obj = new WrapperPlayServerScoreboardObjective();
        obj.setMode(0);
        obj.setObjectiveName(name);
        obj.setObjectiveValue(show);
        result.add(obj.getHandle());

        WrapperPlayServerScoreboardDisplayObjective displayObj = new WrapperPlayServerScoreboardDisplayObjective();
        displayObj.setPosition(1);
        displayObj.setScoreName(name);
        result.add(displayObj.getHandle());

        for (int index=0;index<froms.size();index++) {
            WrapperPlayServerScoreboardScore score = new WrapperPlayServerScoreboardScore();
            score.setObjectiveName(name);
            score.setScoreboardAction(EnumWrappers.ScoreboardAction.CHANGE);
            score.setScoreName(froms.get(index)+getEnd(index));
            score.setValue(index);
            result.add(score.getHandle());
        }

        return result;
    }

    /**
     * 获取更新侧边栏标题信息包
     * @param name 玩家名(用作objective的唯一标识),不为null
     * @param show 显示的标题(如果超过32字符会自动截取前32字符),可为null
     */
    private PacketContainer getUpdateSideTitlePacket(String name, String show) {
        if (show == null) show = "";

        WrapperPlayServerScoreboardObjective obj = new WrapperPlayServerScoreboardObjective();
        obj.setMode(2);
        obj.setObjectiveName(name);
        obj.setObjectiveValue(show);//bug,无效
        obj.getHandle().getStrings().write(1, show);//因为上面的bug,使用此方法
        return obj.getHandle();
    }

    /**
     * 获取更新侧边栏信息包
     * @param name 玩家名(用作objective的唯一标识),不为null
     * @param index 位置,从下往上排,0-(sideSize-1)
     * @param from 发出者,最长14字符,可为null
     */
    private PacketContainer getUpdateSideItemPacket(String name, int index, String from) {
        if (from == null) from = "";
        from = from.substring(0, Math.min(14, from.length()));
        from += getEnd(index);

        WrapperPlayServerScoreboardScore score = new WrapperPlayServerScoreboardScore();
        score.setObjectiveName(name);
        score.setScoreboardAction(EnumWrappers.ScoreboardAction.CHANGE);
        score.setScoreName(from);
        score.setValue(index);
        return score.getHandle();
    }

    /**
     * 获取删除侧边栏信息包
     * @param name 玩家名(用作objective的唯一标识),不为null
     * @param index 位置,从下往上排,0-(sideSize-1)
     * @param from 发出者,最长14字符,可为null
     */
    private PacketContainer getRemoveSideItemPacket(String name, int index, String from) {
        if (from == null) from = "";
        from = from.substring(0, Math.min(14, from.length()));
        from += getEnd(index);

        WrapperPlayServerScoreboardScore score = new WrapperPlayServerScoreboardScore();
        score.setObjectiveName(name);
        score.setScoreboardAction(EnumWrappers.ScoreboardAction.REMOVE);
        score.setScoreName(from);
        score.setValue(index);
        return score.getHandle();
    }

    /**
     * 获取去除侧边栏显示包
     * @param name 玩家名(用作objective的唯一标识),不为null
     */
    private PacketContainer getRemoveSidePacket(String name) {
        WrapperPlayServerScoreboardObjective obj = new WrapperPlayServerScoreboardObjective();
        obj.setMode(1);
        obj.setObjectiveName(name);
        return obj.getHandle();
    }

    /**
     * 发送包
     * @param p 玩家
     * @param pc 包
     */
    private void send(Player p, PacketContainer pc) {
        try {
            protocolManager.sendServerPacket(p, pc);
        } catch (InvocationTargetException e) {
            //do nothing
        }
    }

    /**
     * 获取玩家的信息
     * @param name 玩家名,不为null
     */
    private MsgInfo getMsgInfo(String name) {
        MsgInfo msgInfo = msgInfos.get(name);
        if (msgInfo == null) {
            msgInfo = new MsgInfo(null, null);
            msgInfos.put(name, msgInfo);
        }
        return msgInfo;
    }

    /**
     * 获取玩家的侧边栏信息
     * @param name 玩家名,不为null
     */
    private SideInfo getShowInfo(String name) {
        SideInfo sideInfo = shows.get(name);
        if (sideInfo == null) {
            List<String> froms = new ArrayList<>();
            for (int i=0;i<scoreboardConfig.getSideSize();i++) froms.add("");

            sideInfo = new SideInfo("", froms);
            shows.put(name, sideInfo);
        }
        return sideInfo;
    }

    /**
     * 获取唯一的颜色结尾(两个字符),用来防止内容相同
     * @param index 位置,0-(sideSize-1)
     */
    private String getEnd(int index) {
        index = index%colors.length();
        char c = colors.charAt(index);
        return "\u00A7"+c;
    }
}
