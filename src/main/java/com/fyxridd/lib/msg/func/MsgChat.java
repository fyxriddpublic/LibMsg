package com.fyxridd.lib.msg.func;

import com.fyxridd.lib.core.api.MessageApi;
import com.fyxridd.lib.core.api.fancymessage.FancyMessage;
import com.fyxridd.lib.msg.model.LevelData;
import com.fyxridd.lib.speed.api.SpeedApi;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.fyxridd.lib.core.api.PerApi;
import com.fyxridd.lib.core.api.config.ConfigApi;
import com.fyxridd.lib.core.api.config.Setter;
import com.fyxridd.lib.func.api.func.Func;
import com.fyxridd.lib.func.api.func.FuncType;
import com.fyxridd.lib.msg.MsgPlugin;
import com.fyxridd.lib.msg.api.MsgApi;
import com.fyxridd.lib.msg.config.MsgConfig;
import com.fyxridd.lib.show.chat.api.ShowApi;
import com.fyxridd.lib.show.chat.api.show.PlayerContext;
import com.fyxridd.lib.show.chat.api.show.Refresh;
import com.fyxridd.lib.show.chat.api.show.ShowList;

@FuncType("chat")
public class MsgChat implements Refresh{
    private static final String PAGE_MAIN = "Main";
    private static final String PAGE_PREFIX = "Prefix";
    private static final String PAGE_SUFFIX = "Suffix";
    private static final String SHORT_SEL = "st_sel";

    private MsgConfig config;
    
    public MsgChat() {
        //添加配置监听
        ConfigApi.addListener(MsgPlugin.instance.pn, MsgConfig.class, new Setter<MsgConfig>() {
            @Override
            public void set(MsgConfig value) {
                config = value;
            }
        });
    }
    
    @Func("main")
    public void showMain(CommandSender sender) {
        if (!(sender instanceof Player)) return;
        Player p = (Player) sender;
        
        showMain(p, null);
    }

    @Func("prefix")
    public void showPrefix(CommandSender sender) {
        if (!(sender instanceof Player)) return;
        Player p = (Player) sender;
        
        showPrefix(p, null);
    }

    @Func("suffix")
    private void showSuffix(CommandSender sender) {
        if (!(sender instanceof Player)) return;
        Player p = (Player) sender;
        
        showSuffix(p, null);
    }

    @Func("sel")
    private void selShow(Player p, String type) {
        //检测权限
        if (!checkPer(p, type)) return;
        //速度检测
        if (!SpeedApi.checkShort(p, MsgPlugin.instance.pn, SHORT_SEL, 3)) return;
        //与当前显示相同
        boolean isPrefix = MsgPlugin.instance.getMsgManager().isPrefix(type);
        if (type.equals(MsgApi.getNowType(p.getName(), isPrefix))) {
            MessageApi.send(p, get(p.getName(), 70), true);
            return;
        }
        //无此称号
        String level = MsgApi.getLevel(p.getName(), type);
        if (level == null) {
            if (isPrefix) MessageApi.send(p, get(p.getName(), 40), true);
            else MessageApi.send(p, get(p.getName(), 45), true);
            return;
        }
        //选择显示成功
        MsgPlugin.instance.getMsgManager().setLevel(p.getName(), type, level, isPrefix, true);
        //提示
        if (isPrefix) MessageApi.send(p, get(p.getName(), 50, level), true);
        else MessageApi.send(p, get(p.getName(), 55, level), true);
    }

    @Func("cancel")
    private void cancel(Player p, boolean prefix) {
        //检测权限
        if (!checkPer(p, prefix)) return;
        //速度检测
        if (!SpeedApi.checkShort(p, MsgPlugin.instance.pn, SHORT_SEL, 3)) return;
        //取消显示成功
        MsgPlugin.instance.getMsgManager().setLevel(p.getName(), null, null, prefix, true);
        //提示
        if (prefix) MessageApi.send(p, get(p.getName(), 60), true);
        else MessageApi.send(p, get(p.getName(), 65), true);
    }

    public void showMain(Player p, PlayerContext pc) {
        if (pc != null) ShowApi.show(this, "main", p, MsgPlugin.instance.pn, PAGE_MAIN, null, pc.getPageNow(), pc.getListNow(), null, null);
        else ShowApi.show(this, "main", p, MsgPlugin.instance.pn, PAGE_MAIN, null, 1, 1, null, null);
    }

    private void showPrefix(Player p, PlayerContext pc) {
        //list
        ShowList showList = ShowApi.getShowList(2, MsgPlugin.instance.getMsgManager().getPrefixs(p.getName()), LevelData.class);
        //显示
        if (pc != null) ShowApi.show(this, "prefix", p, MsgPlugin.instance.pn, PAGE_PREFIX, showList, pc.getPageNow(), pc.getListNow(), null, null);
        else ShowApi.show(this, "prefix", p, MsgPlugin.instance.pn, PAGE_PREFIX, showList, 1, 1, null, null);
    }

    private void showSuffix(Player p, PlayerContext pc) {
        //list
        ShowList showList = ShowApi.getShowList(2, MsgPlugin.instance.getMsgManager().getSuffixs(p.getName()), LevelData.class);
        //显示
        if (pc != null) ShowApi.show(this, "suffix", p, MsgPlugin.instance.pn, PAGE_SUFFIX, showList, pc.getPageNow(), pc.getListNow(), null, null);
        else ShowApi.show(this, "suffix", p, MsgPlugin.instance.pn, PAGE_SUFFIX, showList, 1, 1, null, null);
    }

    @Override
    public void refresh(PlayerContext pc) {
        if (pc.getObj() != null) {
            String[] args = pc.getObj().toString().split(" ");
            switch (args.length) {
                case 1:
                    if (args[0].equalsIgnoreCase("main")) showMain(pc.getP(), pc);
                    else if (args[0].equalsIgnoreCase("prefix")) showPrefix(pc.getP(), pc);
                    else if (args[0].equalsIgnoreCase("suffix")) showSuffix(pc.getP(), pc);
                    break;
            }
        }
    }

    /**
     * 检测权限
     */
    private boolean checkPer(Player p, String type) {
        return checkPer(p, MsgPlugin.instance.getMsgManager().isPrefix(type));
    }

    /**
     * 检测权限
     */
    private boolean checkPer(Player p, boolean prefix) {
        if (prefix) {
            if (!PerApi.checkHasPer(p.getName(), config.getPrefixPer())) return false;
        }else {
            if (!PerApi.checkHasPer(p.getName(), config.getSuffixPer())) return false;
        }
        return true;
    }

    private FancyMessage get(String player, int id, Object... args) {
        return config.getLang().get(player, id, args);
    }
}
