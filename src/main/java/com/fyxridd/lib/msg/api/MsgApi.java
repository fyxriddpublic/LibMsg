package com.fyxridd.lib.msg.api;

import org.bukkit.entity.Player;

public class MsgApi {
    /**
     * 注册前后缀
     * @param type 类型名
     * @param prefix true表示显示在前缀;false表示显示在后缀
     */
    public static void registerLevel(String type, boolean prefix) {
        MsgMain.msgManager.registerLevel(type, prefix);
    }

    /**
     * 获取玩家的前后缀
     * @param name 玩家名
     * @param type 类型名
     * @return 前后缀,可为null
     */
    public static String getLevel(String name, String type) {
        return MsgMain.msgManager.getLevel(name, type);
    }

    /**
     * 设置玩家前后缀
     * @param name 玩家名,不为null
     * @param type 类型名,不为null
     * @param level 前后缀,最长16字符,超过会被截断,null或""表示删除
     */
    public static void setLevel(String name, String type, String level) {
        MsgMain.msgManager.setLevel(name, type, level);
    }

    /**
     * 获取玩家当前显示的类型
     * @param name 玩家名
     * @param prefix 是否前缀
     * @return 当前显示的类型,可为null
     */
    public static String getNowType(String name, boolean prefix) {
        return MsgMain.msgManager.getNowType(name, prefix);
    }

    /**
     * 获取玩家当前显示的前缀
     * @param name 玩家名,可为null(null时返回null)
     * @return 前缀,不存在或异常返回null或""
     */
    public static String getPrefix(String name) {
        return MsgMain.getPrefix(name);
    }

    /**
     * 获取玩家当前显示的后缀
     * @param name 玩家名,可为null(null时返回null)
     * @return 后缀,不存在或异常返回null或""
     */
    public static String getSuffix(String name) {
        return MsgMain.getSuffix(name);
    }

    /**
     * 检测玩家是否显示侧边栏
     * @param p 玩家,可为null(null时返回false)
     * @return 是否显示侧边栏
     */
    public static boolean isDisplaySideBar(Player p) {
        return MsgMain.isDisplaySideBar(p);
    }

    /**
     * 开启/关闭玩家的侧边栏显示
     * @param p 玩家,可为null(null或不在线时无效果)
     * @param display 是否显示
     */
    public static void setDisplaySideBar(Player p, boolean display) {
        MsgMain.setDisplaySideBar(p, display);
    }

    /**
     * 注册侧边栏值获取器
     * @param name 获取器名,唯一
     * @param sideHandler 值获取器
     */
    public static void registerSideHandler(String name, SideHandler sideHandler) {
        MsgMain.registerSideHandler(name, sideHandler);
    }

    /**
     * 更新玩家的侧边栏显示
     * @param name 获取器名,唯一
     */
    public static void updateSideShow(Player p, String name) {
        MsgMain.updateSideShow(p, name);
    }
}
