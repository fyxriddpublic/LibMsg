package com.fyxridd.lib.msg.func;

import com.fyxridd.lib.func.api.func.Func;
import com.fyxridd.lib.func.api.func.FuncType;
import com.fyxridd.lib.msg.MsgPlugin;
import com.fyxridd.lib.show.chat.api.ShowApi;
import com.fyxridd.lib.show.chat.api.show.PlayerContext;
import com.fyxridd.lib.show.chat.api.show.Refresh;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@FuncType("chat")
public class MsgChat implements Refresh{
    private static final String PAGE_MAIN = "Main";
    private static final String PAGE_PREFIX = "Prefix";
    private static final String PAGE_SUFFIX = "Suffix";

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

    public void showMain(Player p, PlayerContext pc) {
        if (pc != null) ShowApi.show(this, "main", p, MsgPlugin.instance.pn, PAGE_MAIN, pc.getPageNow(), pc.getListNow(), null, null);
        else ShowApi.show(this, "main", p, MsgPlugin.instance.pn, PAGE_MAIN, 1, 1, null, null);
    }

    private void showPrefix(Player p, PlayerContext pc) {
        if (pc != null) ShowApi.show(this, "prefix", p, MsgPlugin.instance.pn, PAGE_PREFIX, pc.getPageNow(), pc.getListNow(), null, null);
        else ShowApi.show(this, "prefix", p, MsgPlugin.instance.pn, PAGE_PREFIX, 1, 1, null, null);
    }

    private void showSuffix(Player p, PlayerContext pc) {
        if (pc != null) ShowApi.show(this, "suffix", p, MsgPlugin.instance.pn, PAGE_SUFFIX, pc.getPageNow(), pc.getListNow(), null, null);
        else ShowApi.show(this, "suffix", p, MsgPlugin.instance.pn, PAGE_SUFFIX, 1, 1, null, null);
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
}
