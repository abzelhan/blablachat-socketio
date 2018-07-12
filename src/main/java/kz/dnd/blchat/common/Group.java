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

/**
 *
 * @author bakhyt
 */
public class Group {

    long id;
    String room;
    String uuid;
    String publicTitle;
    boolean isPrivate;
    String owner;
    List<User> users;
    String owner_username;
    String owner_nickname;

    Image image;
    int participants;
    boolean isInMyList;

    public Group() {
    }

    public Group(String room) {
        this.room = room;
        this.uuid = room;
    }

    public ObjectNode getJson() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode o = mapper.createObjectNode();
        o.put("room", room);
        o.put("publicTitle", publicTitle);
        o.put("isPrivate", isPrivate);

        o.put("usersCount", participants);
        o.put("isInMyList", isInMyList);

        ObjectNode u = mapper.createObjectNode();
        u.put("username", owner_username);
        u.put("nickname", owner_nickname);

        o.set("owner", u);

        if (image != null) {
            ObjectNode i = mapper.createObjectNode();
            i.put("code", image.getCode());
            i.put("big", image.getBig());

            o.set("image", i);
        }

        if (users != null && !users.isEmpty()) {
            ArrayNode arr = mapper.createArrayNode();
            for (User user : users) {
                ObjectNode ob = mapper.createObjectNode();
                ob.put("username", user.getUsername());
                ob.put("nickname", user.getNickname());

                if (user.isIsAdmin()) {
                    ob.put("isAdmin", true);
                }
                if (user.getImage() != null) {
                    ObjectNode i = mapper.createObjectNode();
                    i.put("code", user.getImage().getCode());
                    i.put("big", user.getImage().getBig());

                    ob.set("image", i);
                }

                arr.add(ob);
            }
            o.putArray("users").addAll(arr);
        }

        return o;
    }

    public int getParticipants() {
        return participants;
    }

    public void setParticipants(int participants) {
        this.participants = participants;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPublicTitle() {
        return publicTitle;
    }

    public void setPublicTitle(String publicTitle) {
        this.publicTitle = publicTitle;
    }

    public boolean isIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public String getOwner_username() {
        return owner_username;
    }

    public void setOwner_username(String owner_username) {
        this.owner_username = owner_username;
    }

    public String getOwner_nickname() {
        return owner_nickname;
    }

    public void setOwner_nickname(String owner_nickname) {
        this.owner_nickname = owner_nickname;
    }

    public Image getImage() {
        if (image == null) {
            image = new Image();
            image.setCode("MTUwNTQ1NTM2NTI4MQ");
            image.setBig("http://blablachat.me/img?code=MTUwNTQ1NTM2NTI4MQ");
        }
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public boolean isIsInMyList() {
        return isInMyList;
    }

    public void setIsInMyList(boolean isInMyList) {
        this.isInMyList = isInMyList;
    }

    @Override
    public String toString() {
        return "Group{" + "id=" + id + ", room=" + room + ", uuid=" + uuid + ", publicTitle=" + publicTitle + ", isPrivate=" + isPrivate + ", owner=" + owner + ", owner_username=" + owner_username + ", owner_nickname=" + owner_nickname + ", image=" + image + '}';
    }

}
