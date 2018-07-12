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
public class DeviceToken {
    String token;
    String source;
    String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return "DeviceToken{" + "token=" + token + ", source=" + source + ", username=" + username + '}';
    }
    
    
    
}
