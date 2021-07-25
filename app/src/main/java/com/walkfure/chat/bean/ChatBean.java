package com.walkfure.chat.bean;


public class ChatBean {
    private String img;
    private String id;
    private String name;
    private String chatMessage;
    private boolean isMeSend;
    private int chatType;
    private long time;
    private boolean isDisplayTime;
    private String VideoUrl = "";

    public String getVideo() {
        return VideoUrl;
    }

    public void setVideo(String yes) {
        this.VideoUrl = yes;
    }

    ;

    public String getImg() {
        return img;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;

    }

    public ChatBean(String id) {
        this.id = id;
    }

    public boolean getIsDisplayTime() {
        return isDisplayTime;
    }

    public void setIsDisplayTime(boolean isDisplayTime) {
        this.isDisplayTime = isDisplayTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getChatType() {
        return chatType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setChatType(int chatType) {
        this.chatType = chatType;
    }

    public String getChatMessage() {
        return chatMessage;
    }

    public void setChatMessage(String chatMessage) {
        this.chatMessage = chatMessage;
    }

    public boolean isMeSend() {
        return isMeSend;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public void setMeSend(boolean isMeSend) {
        this.isMeSend = isMeSend;
    }

    public ChatBean() {
        super();
    }


}
