/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kz.dnd.blchat.common;

/**
 *
 * @author bakhyt
 */

public class ChatObject {
    private String to;
    private String from;
    private String id;
    private String type;
    private String body;
    private String mimetype;
    private String timestamp;
    private String event;
    private String room;
    private boolean enc;

    public ChatObject() {
    }

    public ChatObject applyEvent(String event) {
        this.setEvent(event);
        if (!event.equalsIgnoreCase("chatevent")){
            type = "";
            body = "";
            mimetype = "";
            timestamp = "";
        }
        return this;
    }

    public String getEvent() {
        if (event == null || event.isEmpty()) {
            event = "chatevent";
        }
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getMimetype() {
        return mimetype;
    }

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    
    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public boolean isEnc() {
        return enc;
    }

    public void setEnc(boolean enc) {
        this.enc = enc;
    }

    
    @Override
    public String toString() {
        return "ChatObject{" + "to=" + to + ", from=" + from + ", id=" + id + ", type=" + type + ", body=" + body + ", mimetype=" + mimetype + ", timestamp=" + timestamp + ", event=" + event + ", room=" + room + '}';
    }
    
    
    
    

}
