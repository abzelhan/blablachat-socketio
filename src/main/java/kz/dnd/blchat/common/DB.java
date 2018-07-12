/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kz.dnd.blchat.common;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import kz.dnd.blchat.utils.BaseX;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kz.dnd.blchat.utils.PwGen;
import kz.dnd.pushservice.pojo.PushNotification;

/**
 *
 * @author bakhyt.kudaybergenov <bakhyt@intellictech.com>
 */
public class DB {
    
    private Connection con;
    String url, name, password;
    
    private synchronized boolean open() {
        try {
            if (con != null && !con.isClosed()) {
                return true;
                
            }
            try {
                Class.forName("com.mysql.jdbc.Driver");
                con = DriverManager.getConnection(url + "&serverTimezone=UTC", name, password);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (con == null || con.isClosed()) {
                return false;
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public DB(String url, String name, String password) {
        this.url = url;
        this.name = name;
        this.password = password;
    }

    /* User Management Stuff */
    public boolean checkNickname(String nickname) throws Exception {
        
        boolean result = false;
        open();
        
        PreparedStatement ps = con.prepareStatement("select id from users where nickname=?");
        ps.setString(1, nickname);
        ResultSet rs = ps.executeQuery();
        result = rs.next();
        rs.close();
        ps.close();
        return result;
    }
    
    public boolean authUser(String username, String password) throws Exception {
        
        boolean result = false;
        open();
        
        PreparedStatement ps = con.prepareStatement("select id from users where username=? and jpassword=?");
        ps.setString(1, username);
        ps.setString(2, password);
        ResultSet rs = ps.executeQuery();
        
        result = rs.next();
        
        rs.close();
        ps.close();
        return result;
    }
    
    public User registerUser(User user) throws Exception {
        
        User r = new User();
        open();
        
        r = getUserByPhone(user.getPhone());
        
        if (r == null) {
            r = new User();
            
            PreparedStatement ps = con.prepareStatement("select count(*) from users");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                
                long id = rs.getLong(1);
                BaseX base = new BaseX(BaseX.DICTIONARY_32);
                String userCode = base.encode(BigInteger.valueOf(id)).toLowerCase();
                
                r.setUsername(userCode);
                r.setNickname(user.getNickname());
                r.setPhone(user.getPhone());
                r.setPassword(PwGen.getPassword(8));
                r = saveUser(r);
            }

            //generating JID
            /*PreparedStatement ps = con.prepareStatement("update users set username=? where id=?");
            ps.setString(1, userCode);
            ps.setLong(2, user.getId());
            ps.executeUpdate();*/
            ps.close();
            
        }
        return r;
    }
    
    public void setLastActivity(String username) throws Exception {
        
        open();
        
        PreparedStatement ps = con.prepareStatement("update users set lastActivity=? where username=?");
        ps.setLong(1, System.currentTimeMillis());
        ps.setString(2, username);
        ps.executeUpdate();
        
        ps.close();
        
    }
    
    public List<User> getRosters(String username) throws Exception {
        
        List<User> list = new ArrayList<>();
        open();
//fromUsername,toUsername
        PreparedStatement ps = con.prepareStatement("select u.username,u.nickname,u.lastActivity,u.id from users u, rosters r "
                + "where r.blockedBy is null and ((r.fromUsername=? and r.toUsername=u.username) or "
                + " (r.toUsername=? and r.fromUsername=u.username))");
        ps.setString(1, username);
        ps.setString(2, username);
        
        ResultSet rs = ps.executeQuery();
        
        while (rs.next()) {
            User u = new User();
            u.setUsername(rs.getString(1));
            u.setNickname(rs.getString(2));
            u.setLastActivity(rs.getLong(3));
            u.setId(rs.getLong(4));
            
            if (!list.contains(u)) {
                list.add(u);
            }
        }
        rs.close();
        ps.close();
        return list;
    }
    
    public List<User> syncUsers(List<String> phones) throws Exception {
        
        List<User> list = new ArrayList<>();
        open();
        
        StringBuilder sb = new StringBuilder();
        String delim = "";
        Map<String, String> phoneMap = new HashMap<String, String>();
        
        if (phones != null && !phones.isEmpty()) {
            
            for (String phone : phones) {
                String phoneE = phone.replaceAll("[^\\d.]", "");
                
                if (!phoneE.isEmpty()) {
                    if (phoneE.startsWith("87")) {
                        phoneE = phoneE.replaceFirst("87", "77");
                    }
                    
                    sb.append(delim).append(phoneE);
                    
                    phoneMap.put(phoneE, phone);
                    
                    delim = ",";
                    
                }
            }
            
            System.out.println("result phones: " + sb.toString());
            
            PreparedStatement ps = con.prepareStatement("select u.username,u.nickname,u.phone,f.code from users u"
                    + " left join files f on f.id=u.image_id "
                    + " where "
                    + "u.phone not like '00%' and u.phone in (" + sb.toString() + ") ");
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                User u = new User();
                u.setUsername(rs.getString("username"));
                u.setNickname(rs.getString("nickname"));
                u.setSearchPhone(phoneMap.get(rs.getString("phone")));
                
                Image ava = new Image();
                ava.setCode(rs.getString(4));
                ava.setBig("http://blablachat.me/img?code=" + ava.getCode());
                u.setImage(ava);
                
                list.add(u);
            }
            rs.close();
            ps.close();
        }
        return list;
    }
    
    public User getUserByPhone(String phone) throws Exception {
        User r = null;
        open();
        
        phone = phone.replaceAll("[^\\d.]", "");
        
        PreparedStatement ps = con.prepareStatement("select u.id,u.username,u.jpassword,u.nickname,f.code,u.gender,u.interestedGenders "
                + " from users u "
                + " left join files f on u.image_id=f.id "
                + " where u.phone=? ");
        ps.setString(1, phone);
        ResultSet rs = ps.executeQuery();
        
        if (rs.next()) {
            r = new User();
            r.setId(rs.getLong(1));
            r.setUsername(rs.getString(2));
            r.setPassword(rs.getString(3));
            r.setNickname(rs.getString(4));
            Image image = new Image();
            image.setCode(rs.getString(5));
            image.setBig("http://blablachat.me/img?code=" + image.getCode());
            r.setImage(image);
            
            r.setGender(rs.getString(6));
            List<String> interestedGenders = new ArrayList<>();
            try {
                String[] arr = rs.getString(7).split(",");
                for (String a : arr) {
                    interestedGenders.add(a);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            r.setInterestedGenders(interestedGenders);
        }
        
        rs.close();
        ps.close();
        return r;
    }
    
    public User getUserByUsername(String username) throws Exception {
        User r = null;
        open();
        
        PreparedStatement ps = con.prepareStatement("select u.id,u.username,u.jpassword,u.nickname,f.code from users u "
                + " left join files f on f.id=u.image_id "
                + " where u.username=?");
        ps.setString(1, username);
        ResultSet rs = ps.executeQuery();
        
        if (rs.next()) {
            r = new User();
            r.setId(rs.getLong(1));
            r.setUsername(rs.getString(2));
            r.setPassword(rs.getString(3));
            r.setNickname(rs.getString(4));
            
            Image image = new Image();
            image.setCode(rs.getString(5));
            image.setBig("http://blablachat.me/img?code=" + image.getCode());
            r.setImage(image);
            
        }
        
        rs.close();
        ps.close();
        return r;
    }
    
    public User saveUser(User user) throws Exception {
        open();
        
        if (user.getId() > 0) {
            
            String interestedGenders = "";
            String delim = "";
            if (user.getInterestedGenders() != null) {
                for (String gender : user.getInterestedGenders()) {
                    interestedGenders += delim + gender;
                    delim = ",";
                }
            }
            
            System.out.println("saveUser: [" + user.getUsername() + "]->" + " saving as existing user: " + user.getId());
            
            PreparedStatement ps = con.prepareStatement("update users set nickname=?,image_id=(select id from files where code=?),gender=?,interestedGenders=? "
                    + " where id=?");
            ps.setString(1, user.getNickname());
            ps.setString(2, user.getImage().getCode());
            ps.setString(3, user.getGender());
            ps.setString(4, interestedGenders);
            ps.setLong(5, user.getId());
            
            System.out.println("saveUser: [" + user.getUsername() + "]->" + " saving as existing user: " + ps.executeUpdate());
            
            ps.close();
        } else {
            System.out.println("saveUser: [" + user.getUsername() + "]->" + " creating new user ");
            PreparedStatement ps = con.prepareStatement("insert into users(phone,username, jpassword,nickname,created) values(?,?,?,?,now())", PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getPhone());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getNickname());
            
            System.out.println("saveUser: [" + user.getUsername() + "]->" + " creating new user: " + ps.executeUpdate());
            
            ResultSet rs = ps.getGeneratedKeys();
            
            if (rs.next()) {
                user.setId(rs.getLong(1));
            }
            
            ps.close();
        }
        
        return user;
    }
    
    public void addDeviceToken(DeviceToken data) throws Exception {
        open();
        
        PreparedStatement ps = con.prepareStatement("update deviceTokens set deleted=1 where token=?");
        ps.setString(1, data.getToken());
        ps.executeUpdate();
        
        
        ps = con.prepareStatement("select * from deviceTokens"
                + " where token=? and source=? and user_id=(select id from users where username=? limit 0,1)");
        ps.setString(1, data.getToken());
        ps.setString(2, data.getSource());
        ps.setString(3, data.getUsername());
        
        ResultSet rs = ps.executeQuery();
        
        if (!rs.next()) {
            ps = con.prepareStatement("insert into deviceTokens"
                    + "(created,token,source,user_id) values(now(),?,?,(select id from users where username=?))");
            ps.setString(1, data.getToken());
            ps.setString(2, data.getSource());
            ps.setString(3, data.getUsername());
            
            ps.executeUpdate();
        }
        
        rs.close();
        ps.close();
    }
    
    public void removeDeviceToken(DeviceToken data) throws Exception {
        open();
        PreparedStatement ps = con.prepareStatement("update deviceTokens "
                + " set deleted=1 where token=? and user_id=(select id from users where username=? limit 0,1)");
        ps.setString(1, data.getToken());
        ps.setString(2, data.getUsername());
        
        ps.executeUpdate();
        
        ps.close();
    }


    /*End of User Management Stuff*/

 /*Offline messages stuff*/
    public void archive(ChatObject data, boolean sendPush, boolean addToOffline) throws Exception {
        open();
        if (addToOffline) {
            addOfflineMessage(data);
        }
        
        if (sendPush) {
            sendPush(data);
        }
    }
    
    public List<ChatObject> getOfflineMessages(String username) throws Exception {
        List<ChatObject> list = new ArrayList<>();
        open();
        PreparedStatement ps = con.prepareStatement("select distinct message_id,id,to_user_id,from_user_id,message,event,mimetype,timestamp,room,enc "
                + "from offline_messages where "
                + "taken=0 and "
                + "((to_user_id=? and event='chatevent') "
                + "or (from_user_id=? and event<>'chatevent')) ");
        ps.setString(1, username);
        ps.setString(2, username);
        
        ResultSet rs = ps.executeQuery();
        
        StringBuilder sb = new StringBuilder();
        String delim = "";
        
        while (rs.next()) {
            
            ChatObject p = new ChatObject();
            p.setId(rs.getString("message_id"));
            p.setTo(rs.getString("to_user_id"));
            p.setFrom(rs.getString("from_user_id"));
            p.setEnc(rs.getBoolean("enc"));
            try {
                if (p.isEnc()) {
                    try {
                        p.setBody(new String(Base64.getDecoder().decode(rs.getString("message"))));
                        p.setEnc(false);
                    } catch (Exception e) {
                        p.setBody(rs.getString("message")); 
                    }
                } else {
                    p.setBody(rs.getString("message"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                p.setBody(rs.getString("message"));
            }
            p.setEvent(rs.getString("event"));
            p.setMimetype(rs.getString("mimetype"));
            p.setTimestamp(rs.getString("timestamp"));
            p.setRoom(rs.getString("room"));
            
            list.add(p);
            
            sb.append(delim).append(rs.getLong("id"));
            delim = ",";
        }
        rs.close();
        System.out.println("taken: " + sb.toString());
        
        if (!sb.toString().isEmpty()) {
            ps = con.prepareStatement("update offline_messages set taken=1 where id in (" + sb.toString() + ")");
            ps.executeUpdate();
        }
        
        ps.close();
        return list;
    }
    
    public void addOfflineMessage(ChatObject data) throws Exception {
        open();
        PreparedStatement ps = con.prepareStatement("insert into offline_messages"
                + "(message_id,to_user_id,from_user_id,message,mimetype,timestamp,event,room,enc) values(?,?,?,?,?,?,?,?,?)");
        ps.setString(1, data.getId());
        ps.setString(2, data.getTo());
        ps.setString(3, data.getFrom());
        if (data.isEnc()) {
            ps.setString(4, data.getBody());
        } else {
            if (data.getBody() == null) {
                data.setBody("");
            }
            data.setBody(new String(Base64.getEncoder().encode(data.getBody().getBytes())));
            ps.setString(4, data.getBody());
            data.setEnc(true);
        }
        ps.setString(5, data.getMimetype());
        ps.setString(6, data.getTimestamp());
        ps.setString(7, data.getEvent());
        ps.setString(8, data.getRoom());
        ps.setBoolean(9, data.isEnc());
        
        ps.executeUpdate();
        
        ps.close();
    }
    
    public void sendPush(ChatObject data) throws Exception {
        open();
        PreparedStatement ps = con.prepareStatement("insert into push_notification"
                + "(message_id,to_user_id,from_user_id,message,event,mimetype,timestamp,room,roomBadge,allBadge) "
                + "values(?,?,?,?,?,?,?,?,(select count(*) from offline_messages where to_user_id=? and room=? and taken=0),"
                + "(select count(*) from offline_messages where to_user_id=? and event='chatevent' and taken=0))");
        
        ps.setString(1, data.getId());
        ps.setString(2, data.getTo());
        ps.setString(3, data.getFrom());
        if (data.getMimetype().startsWith("image")) {
            ps.setString(4, new String(Base64.getEncoder().encode("🏞 Изображение".getBytes())));
        } else if (data.getMimetype().startsWith("audio")) {
            ps.setString(4, new String(Base64.getEncoder().encode("📢 Аудио".getBytes())));
        } else {
            if (data.isEnc()) {
                ps.setString(4, data.getBody());
            } else {
                ps.setString(4, new String(Base64.getEncoder().encode(data.getBody().getBytes())));
            }
        }
        ps.setString(5, data.getEvent());
        ps.setString(6, data.getMimetype());
        ps.setString(7, data.getTimestamp());
        ps.setString(8, data.getRoom());
        ps.setString(9, data.getTo());
        ps.setString(10, data.getRoom());
        ps.setString(11, data.getTo());
        
        ps.executeUpdate();
        
        ps.close();
    }
    
    public void sendInvite(String from_username, String to_phone, String lang) throws Exception {
        open();
        
        PreparedStatement ps = con.prepareStatement("insert into invites"
                + "(from_username,to_phone,lang,created) values(?,?,?,now())");
        ps.setString(1, from_username);
        ps.setString(2, to_phone);
        ps.setString(3, lang);
        
        ps.executeUpdate();
        
        ps.close();
    }

    /*END of Offline messages stuff*/
 /*User Relationship Management*/
    public int addRoster(String from, String to, String room) throws Exception {
        open();
        
        int newRoster = -1;
        
        if ((newRoster = isRoster(from, to, room)) == -1) {
            PreparedStatement ps = con.prepareStatement("insert into rosters (fromUsername,toUsername,created,room) values(?,?,now(),?)");
            ps.setString(1, from);
            ps.setString(2, to);
            ps.setString(3, room);
            
            ps.executeUpdate();
            ps.close();
            
        }
        
        return newRoster;
    }
    
    public int isRoster(String from, String to, String room) throws Exception {
        open();
        int result = -1;
        PreparedStatement ps = con.prepareStatement("select blockedBy from rosters where "
                + "((fromUsername=? and toUsername=?) or "
                + "(fromUsername=? and toUsername=?)) and room=?");
        ps.setString(1, from);
        ps.setString(2, to);
        ps.setString(3, to);
        ps.setString(4, from);
        ps.setString(5, room);
        
        ResultSet rs = ps.executeQuery();
        
        if (rs.next()) {
            String blockedBy = rs.getString(1);
            
            if (blockedBy == null || blockedBy.isEmpty()) {
                result = 0;
            } else {
                if (from.equalsIgnoreCase(blockedBy)) {
                    result = 2;
                } else {
                    result = 3;
                }
            }
        }
        
        return result;
    }
    
    public void blockRoster(String from, String to, String room) throws Exception {
        open();
        
        PreparedStatement ps = con.prepareStatement("update rosters set blockedBy=? where "
                + "((fromUsername=? and toUsername=?) or "
                + "(fromUsername=? and toUsername=?)) and room=?");
        
        ps.setString(1, from);
        ps.setString(2, from);
        ps.setString(3, to);
        ps.setString(4, to);
        ps.setString(5, from);
        ps.setString(6, room);
        
        ps.executeUpdate();
        
        ps.close();
    }
    
    public String getRandom(String from) throws Exception {
        open();
        String result = "";
        PreparedStatement ps = con.prepareStatement("select username from users where "
                + " (username not in (select fromUsername from rosters where toUsername=?)) "
                + " and (username not in (select toUsername from rosters where fromUsername=?))"
                + " order by rand()");
        ps.setString(1, from);
        ps.setString(2, from);
        
        ResultSet rs = ps.executeQuery();
        
        if (rs.next()) {
            result = rs.getString(1);
        }
        rs.close();
        ps.close();
        return result;
    }

    /*End of User Relationship Management*/
 /*Groups*/
    public String createGroup(Group group) throws Exception {
        open();
        String uuid = null;
        uuid = java.util.UUID.randomUUID().toString();
        
        while (!isFreeUUID(uuid, "groups")) {
            uuid = java.util.UUID.randomUUID().toString();
            //this can be infinite... but one chance in 17 billions, it's small enough to appear twice, trust me...
        }
        
        System.out.println("group uuid: " + uuid);
        
        PreparedStatement ps = con.prepareStatement("insert into groups"
                + "(created,creator_id,publicTitle,isPrivate,uuid,image_id) "
                + "values(now(),(select id from users where username=? limit 0,1),?,?,?,(select id from files where code=?))");
        
        ps.setString(1, group.getOwner());
        ps.setString(2, new String(Base64.getEncoder().encode(group.getPublicTitle().getBytes())));
        ps.setBoolean(3, group.isIsPrivate());
        ps.setString(4, uuid);
        ps.setString(5, group.getImage().getCode());
        
        System.out.println("create group: " + ps.executeUpdate());
        
        assignAdmin(group.getOwner(), uuid, group.getOwner());
        inviteUser(group.getOwner(), uuid, group.getOwner());
        ps.close();
        
        return uuid;
    }
    
    public boolean saveGroup(Group group) throws Exception {
        open();
        boolean result = false;
        String uuid = group.getUuid();
        System.out.println("group uuid: " + uuid);
        
        PreparedStatement ps = con.prepareStatement("update groups "
                + " set publicTitle=?,image_id=( select id from files where code=? ) "
                + " where uuid=?");
        
        ps.setString(1, new String(Base64.getEncoder().encode(group.getPublicTitle().getBytes())));
        ps.setString(2, group.getImage().getCode());
        ps.setString(3, uuid);
        
        System.out.println("update group: " + (result = (ps.executeUpdate() == 0)));
        
        ps.close();
        
        return result;
    }
    
    public List<Group> getPublicGroups(String caller_username) throws Exception {
        List<Group> list = new ArrayList<>();
        open();
        String query = "select g.publicTitle,g.uuid,f.code,"
                + " (select count(*) from groups_users gu where gu.group_id=g.id),"
                + " (select count(*) from groups_users gu where gu.group_id=g.id and gu.user_id=(select u.id from users u where u.username=? limit 0,1))"
                + " from groups g left join files f on f.id=g.image_id "
                + " where "
                + " g.deleted=0 "
                + " and g.isPrivate=0 ";
        
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, caller_username);
        
        ResultSet rs = ps.executeQuery();
        
        while (rs.next()) {
            Group g = new Group();
            g.setPublicTitle(new String(Base64.getDecoder().decode(rs.getString(1))));
            g.setRoom(rs.getString(2));
            g.setUuid(rs.getString(2));
            
            Image image = new Image();
            image.setCode(rs.getString(3));
            image.setBig("http://blablachat.me/img?code=" + image.getCode());
            g.setImage(image);
            
            g.setParticipants(rs.getInt(4));
            
            g.setIsInMyList(rs.getInt(5) > 0);
            
            list.add(g);
        }
        
        rs.close();
        ps.close();
        
        return list;
    }
    
    public Group getGroup(Group group) throws Exception {
        return getGroup(group, false);
    }
    
    public Group getGroup(Group group, boolean isShort) throws Exception {
        open();
        
        String query = "select g.publicTitle,g.uuid,u.username,u.nickname,f.code from groups g"
                + " left join users u on u.id=g.creator_id "
                + " left join files f on f.id=g.image_id "
                + " where g.deleted=0 and g.uuid=?";
        
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, group.getRoom());
        
        ResultSet rs = ps.executeQuery();
        
        if (rs.next()) {
            group.setPublicTitle(new String(Base64.getDecoder().decode(rs.getString(1))));
            group.setRoom(rs.getString(2));
            group.setOwner_username(rs.getString(3));
            group.setOwner_nickname(rs.getString(4));
            
            Image image = new Image();
            image.setCode(rs.getString(5));
            image.setBig("http://blablachat.me/img?code=" + image.getCode());
            group.setImage(image);
            
            if (!isShort) {
                ps = con.prepareStatement("select distinct u.username,u.nickname,gu.isAdmin,f.code from "
                        + " groups_users gu"
                        + " left join users u on u.id=gu.user_id "
                        + " left join files f on f.id=u.image_id "
                        + " where "
                        + " gu.deleted=0 and "
                        + " gu.group_id=(select id from groups where uuid=? limit 0,1) ");
                
                ps.setString(1, group.getRoom());
                
                rs = ps.executeQuery();
                
                List<User> users = new ArrayList<>();
                while (rs.next()) {
                    User u = new User();
                    u.setUsername(rs.getString(1));
                    u.setNickname(rs.getString(2));
                    u.setIsAdmin(rs.getBoolean(3));
                    
                    Image ava = new Image();
                    ava.setCode(rs.getString(4));
                    ava.setBig("http://blablachat.me/img?code=" + ava.getCode());
                    u.setImage(ava);
                    
                    users.add(u);
                }
                
                group.setUsers(users);
            }
        }
        rs.close();
        ps.close();
        return group;
    }
    
    public boolean canInviteUsers(String username, String group_uuid) throws Exception {
        open();
        boolean isAdmin;
        
        PreparedStatement ps = con.prepareStatement("select * from groups_users gu where "
                + " gu.user_id=(select id from users where username=? limit 0,1) "
                + " and gu.group_id=(select g.id from groups g where g.uuid=? limit 0,1) "
                + " and gu.deleted=0 and gu.isAdmin=1");
        ps.setString(1, username);
        ps.setString(2, group_uuid);
        
        isAdmin = ps.executeQuery().next();
        
        ps.close();
        
        return isAdmin;
    }
    
    public void inviteUser(String creator, String uuid, String user) throws Exception {
        open();
        PreparedStatement ps = con.prepareStatement("select * from groups_users where "
                + "user_id=(select id from users where username=? limit 0,1) "
                + "and group_id=(select id from groups where uuid=? limit 0,1) "
                + "and deleted=0");
        ps.setString(1, user);
        ps.setString(2, uuid);
        
        if (!ps.executeQuery().next()) {
            ps = con.prepareStatement("insert into groups_users"
                    + "(created,creator_id,user_id,group_id) "
                    + "values("
                    + "now(),"
                    + "(select id from users where username=? limit 0,1),"
                    + "(select id from users where username=? limit 0,1),"
                    + "(select id from groups where uuid=? limit 0,1))");
            
            ps.setString(1, creator);
            ps.setString(2, user);
            ps.setString(3, uuid);
            
            ps.executeUpdate();
        }
        
        ps.close();
    }
    
    public void assignAdmin(String creator, String uuid, String user) throws Exception {
        open();
        PreparedStatement ps = con.prepareStatement("update groups_users "
                + " set isAdmin=1 where deleted=0 "
                + " and user_id=(select id from users where username=? limit 0,1 ) "
                + " and group_id=(select id from groups where uuid=? limit 0,1 )");
        
        ps.setString(1, user);
        ps.setString(2, uuid);
        
        ps.executeUpdate();
        
        ps.close();
    }
    
    public void removeAdmin(String creator, String uuid, String user) throws Exception {
        open();
        PreparedStatement ps = con.prepareStatement("update groups_users "
                + " set isAdmin=0 where deleted=0 "
                + " and user_id=( select id from users where username=? limit 0,1 ) "
                + " and group_id=( select id from groups where uuid=? limit 0,1 )");
        
        ps.setString(1, user);
        ps.setString(2, uuid);
        
        ps.executeUpdate();
        
        ps.close();
    }
    
    public void leaveGroup(String creator, String uuid, String user) throws Exception {
        open();
        PreparedStatement ps = con.prepareStatement("update groups_users "
                + "set deleted=1 "
                + " where user_id=(select id from users where username=? limit 0,1 ) "
                + " and group_id=( select id from groups where uuid=? limit 0,1 ) ");
        
        ps.setString(1, user);
        ps.setString(2, uuid);
        
        ps.executeUpdate();
        
        ps.close();
    }
    
    public List<PushNotification> fetch() throws Exception {
        open();
        List<PushNotification> list = new ArrayList<>();
        PreparedStatement ps = con.prepareStatement("select * from push_notification where taken=0 and message<>''");
        ResultSet rs = ps.executeQuery();
        
        StringBuilder sb = new StringBuilder();
        String delim = "";
        
        while (rs.next()) {
            
            PushNotification p = new PushNotification();
            p.setId(rs.getString("message_id"));
            p.setRecipient(rs.getString("to_user_id"));
            p.setFrom(rs.getString("from_user_id"));
            p.setMessage(rs.getString("message"));
            
            list.add(p);
            
            sb.append(delim).append(p.getId());
            delim = ",";
        }
        rs.close();
        System.out.println("taken: " + sb.toString());
        
        if (!sb.toString().isEmpty()) {
            ps = con.prepareStatement("update push_notification set taken=1 where message_id in (" + sb.toString() + ")");
            ps.executeUpdate();
        }
        
        ps.close();
        return list;
    }
    
    public List<String> getTokens(String source, String email) throws Exception {
        open();
        List<String> list = new ArrayList<>();
        PreparedStatement ps = con.prepareStatement("select token from blabla.deviceTokens "
                + "where user_id=(select id from users where username=? limit 0,1) and source=?");
        ps.setString(1, email);
        ps.setString(2, source);
        
        ResultSet rs = ps.executeQuery();
        
        while (rs.next()) {
            list.add(rs.getString(1));
        }
        rs.close();
        ps.close();
        return list;
    }
    
    public boolean isFreeUUID(String uuid, String table) throws Exception {
        boolean result = false;
        open();
        
        PreparedStatement ps = con.prepareStatement("select id from " + table + " where uuid=?");
        ps.setString(1, uuid);
        ResultSet rs = ps.executeQuery();
        result = !rs.next();
        rs.close();
        ps.close();
        return result;
    }
}
