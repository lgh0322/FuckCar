package com.walkfure.chat.bean;

public class CarBean {
    String id = "";
    String name = "";
    String img = "";
    boolean showOption = false;

    public boolean isShowOption() {
        return showOption;
    }

    public void setShowOption(boolean showOption) {
        this.showOption = showOption;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
