package kz.dnd.blchat.listeners.group;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kz.dnd.blchat.common.ChatObject;
import kz.dnd.blchat.common.Group;
import kz.dnd.blchat.common.User;
import kz.dnd.blchat.wrappers.Command;

public class CreateGroupListener implements DataListener<Group> {

    private Command command;

    public CreateGroupListener(Command command) {
        this.command = command;
    }

    @Override
    public void onData(SocketIOClient sioc, Group t,
                       AckRequest ar) throws Exception {
        String username = sioc.getHandshakeData().getSingleUrlParam("username");
        System.out.println("createGroup: [" + username + "]->" + t.toString());
        ObjectNode result = command.ok();

        try {

            System.out.println("createGroup: [" + username + "]->" + "create group from: " + username + " title: " + t.getPublicTitle() + " private: " + t.isIsPrivate());
            t.setOwner(username);

            String uuid = command.getDb().createGroup(t);

            if (uuid == null) {
                System.out.println("createGroup: [" + username + "]->" + "create group 500");
                result = command.error(500, "createGroup: [" + username + "]<-uuid is null");
            } else {
                System.out.println("createGroup: [" + username + "]->" + "create group: " + uuid);
                result = command.ok().put("room", uuid);

                t.setRoom(uuid);

                //here should be called inviteUsers
                try {
                    if (t.getUsers() != null && !t.getUsers().isEmpty()) {
                        System.out.println("inviteToGroup: [" + username + "] can invite to group");
                        for (User user : t.getUsers()) {
                            System.out.println("inviteToGroup: [" + username + "] inviting user: " + user.getUsername());

                            command.getDb().inviteUser(username, t.getRoom(), user.getUsername());

                            ChatObject co = new ChatObject();
                            co.setFrom(username);
                            co.setMimetype("invite");
                            co.setTo(user.getUsername());
                            co.setEvent("invite");
                            co.setRoom(t.getRoom());
                            co.setTimestamp("" + System.currentTimeMillis());
                            command.sendMessage(co, command.getServer(), sioc, command.getDb());

                            co = new ChatObject();
                            co.setBody("Вас добавили в группу");
                            co.setFrom(username);
                            co.setMimetype("service");
                            co.setTo(user.getUsername());
                            co.setEvent("service");
                            co.setRoom(t.getRoom());
                            co.setTimestamp("" + System.currentTimeMillis());
                            command.sendMessage(co, command.getServer(), sioc, command.getDb());

                            Group group = command.getDb().getGroup(t);
                            User currentUser = command.getDb().getUserByUsername(username);
                            String nickname = command.getDb().getUserByUsername(user.getUsername()).getNickname();

                            for (User groupUser : group.getUsers()) {
                                if (!groupUser.getUsername().equalsIgnoreCase(username)
                                        && !groupUser.getUsername().equalsIgnoreCase(user.getUsername())) {
                                    co = new ChatObject();
                                    co.setBody(currentUser.getNickname() + " пригласил " + nickname);
                                    co.setFrom(username);
                                    co.setMimetype("service");
                                    co.setTo(groupUser.getUsername());
                                    co.setEvent("service");
                                    co.setRoom(t.getRoom());
                                    co.setTimestamp("" + System.currentTimeMillis());
                                    command.sendMessage(co, command.getServer(), sioc, command.getDb());

                                }
                            }

                        }
                    } else {
                        System.out.println("inviteToGroup: [" + username + "] there are no initial users");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    result = command.error(500, "inviteToGroup: [" + username + "]<-" + e.getMessage());
                }

                System.out.println("inviteToGroup: [" + username + "]<-" + result.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
            result = command.error(500, "createGroup: [" + username + "]<-" + e.getMessage());
        }

        System.out.println("createGroup: [" + username + "]<-" + result.toString());
        ar.sendAckData(result.toString());

    }
}
