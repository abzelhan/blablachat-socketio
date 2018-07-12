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
public class Presense {
    String username;
    long lastActivity;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(long lastActivity) {
        this.lastActivity = lastActivity;
    }

    @Override
    public String toString() {
        return "Presense{" + "username=" + username + ", lastActivity=" + lastActivity + '}';
    }
}
