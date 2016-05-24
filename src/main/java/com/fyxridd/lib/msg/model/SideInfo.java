package com.fyxridd.lib.msg.model;

import java.util.List;

public class SideInfo {
    private String show;
    private List<String> from;
    public SideInfo(String show, List<String> from) {
        super();
        this.show = show;
        this.from = from;
    }

    public String getShow() {
        return show;
    }

    public void setShow(String show) {
        this.show = show;
    }

    public List<String> getFrom() {
        return from;
    }

    public void setFrom(List<String> from) {
        this.from = from;
    }
}
