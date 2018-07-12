package com.dnd.slack;

/**
 * Created by bakhyt on 9/13/17.
 */
public class SlackAttachmentField {
    String title;
    String value;
    boolean isShort;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isShort() {
        return isShort;
    }

    public void setShort(boolean aShort) {
        isShort = aShort;
    }
}
