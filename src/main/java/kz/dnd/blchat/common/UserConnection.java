/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kz.dnd.blchat.common;

import java.util.UUID;

/**
 *
 * @author bakhyt
 */
public class UserConnection {

    UUID uuid;
    boolean isAway;
    String v;

    public UserConnection(UUID uuid, boolean isAway, String v) {
        this.uuid = uuid;
        this.isAway = isAway;
        if (v != null && !v.isEmpty()) {
            this.v = v;
        } else {
            this.v = "0.1";
        }
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public boolean isIsAway() {
        return isAway;
    }

    public void setIsAway(boolean isAway) {
        this.isAway = isAway;
    }

    public String getV() {
        return v;
    }

    public void setV(String v) {
        this.v = v;
    }

    @Override
    public String toString() {
        return "UserConnection{" + "uuid=" + uuid + ", isAway=" + isAway + ", v=" + v + '}';
    }

}
