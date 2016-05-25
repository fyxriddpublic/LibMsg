package com.fyxridd.lib.msg.func;

import com.fyxridd.lib.core.api.MessageApi;
import com.fyxridd.lib.core.api.PerApi;
import com.fyxridd.lib.core.api.config.ConfigApi;
import com.fyxridd.lib.core.api.config.Setter;
import com.fyxridd.lib.core.api.fancymessage.FancyMessage;
import com.fyxridd.lib.func.api.func.Func;
import com.fyxridd.lib.func.api.func.FuncType;
import com.fyxridd.lib.msg.MsgPlugin;
import com.fyxridd.lib.msg.api.MsgApi;
import com.fyxridd.lib.msg.config.MsgConfig;
import com.fyxridd.lib.speed.api.SpeedApi;
import org.bukkit.entity.Player;

@FuncType("cmd")
public class MsgCmd{
    private static final String SHORT_SEL = "st_sel";

    private MsgConfig config;

    public MsgCmd() {
        //添加配置监听
        ConfigApi.addListener(MsgPlugin.instance.pn, MsgConfig.class, new Setter<MsgConfig>() {
            @Override
            public void set(MsgConfig value) {
                config = value;
            }
        });
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
