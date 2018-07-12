/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kz.dnd.blchat.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author bakhyt
 */
public class User {

    long id;
    String phone;
    String nickname;
    String username;
    String password;
    String lang;
    long lastActivity;
    String searchPhone;
    String gender;
    List<String> interestedGenders;
    boolean isAdmin;
    Image image;

    public User() {
    }

    public User(String username) {
        this.username = username;
    }
    
    public ObjectNode getJson() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode o = mapper.createObjectNode();
        o.put("username", username);
        o.put("nickname", nickname);

        if (image != null) {
            ObjectNode i = mapper.createObjectNode();
            i.put("code", image.getCode());
            i.put("big", image.getBig());

            o.set("image", i);
        }

        return o;
    }

    public Image getImage() {
        if (image == null) {
            image = new Image();
            image.setCode("MTUwNTQ1NTMzODQ3Ng");
            image.setBig("http://blablachat.me/img?code=MTUwNTQ1NTMzODQ3Ng");
        }
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getInsterestedGendersStr() {
        String result = "";
        
        String delim = "";
        
        if (interestedGenders != null && !interestedGenders.isEmpty()) {
            for (String s : interestedGenders) {
                result += delim + s;
                delim = ",";
            }
        }
        
        return result;
    }

    public List<String> getInterestedGenders() {
        return interestedGenders;
    }

    public void setInterestedGenders(List<String> interestedGenders) {
        this.interestedGenders = interestedGenders;
    }

    public long getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(long lastActivity) {
        this.lastActivity = lastActivity;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSearchPhone() {
        return searchPhone;
    }

    public void setSearchPhone(String searchPhone) {
        this.searchPhone = searchPhone;
    }

    public boolean isIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    @Override
    public String toString() {
        return "User{" + "id=" + id + ", phone=" + phone + ", nickname=" + nickname + ", username=" + username + ", password=" + password + ", lang=" + lang + ", lastActivity=" + lastActivity + ", searchPhone=" + searchPhone + ", gender=" + gender + ", interestedGenders=" + interestedGenders + ", isAdmin=" + isAdmin + ", image=" + image + '}';
    }

    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.username);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final User other = (User) obj;
        if (!Objects.equals(this.username, other.username)) {
            return false;
        }
        return true;
    }

}
