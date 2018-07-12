package com.dnd.slack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bakhyt on 9/13/17.
 */
public class SlackAttachment {
    String title;
    String fallback;
    String color;
    String text;

    List<SlackAttachmentField> fields;

    public void addAttachmentField(SlackAttachmentField field){
        if (fields==null){
            fields = new ArrayList<SlackAttachmentField>();
        }
        fields.add(field);
    }

    public List<SlackAttachmentField> getFields() {
        return fields;
    }

    public void setFields(List<SlackAttachmentField> fields) {
        this.fields = fields;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFallback() {
        return fallback;
    }

    public void setFallback(String fallback) {
        this.fallback = fallback;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
