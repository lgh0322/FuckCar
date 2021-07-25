package com.walkfure.chat.bean;

import java.util.ArrayList;

public class ContactsBean {
    private int unRead = 0;
    private String avatar;
    private String name;
    private String phone;
    String time = "";
    String msg = "";
    private ArrayList<ChatBean> msgList = new ArrayList<>();

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getUnRead() {
        return unRead;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return this.time;
    }

    public void setUnRead(int unRead1) {
        this.unRead = unRead1;
    }

    public String getAvatar() {
        return this.avatar;
    }

    public String getName() {
        return this.name;

    }

    public String getMsg() {
        return this.msg;

    }

    public String getPhone() {
        return this.phone;
    }


    public void clearMsg() {
        msgList.clear();
    }

    public void addMsg(ChatBean msg) {
        msgList.add(msg);
    }

    public ArrayList<ChatBean> getMsgList() {
        return msgList;
    }
}
