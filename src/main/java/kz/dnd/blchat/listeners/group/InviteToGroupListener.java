package kz.dnd.blchat.listeners.group;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kz.dnd.blchat.common.ChatObject;
import kz.dnd.blchat.common.Group;
import kz.dnd.blchat.common.User;
import kz.dnd.blchat.wrappers.Command;

public class InviteToGroupListener implements DataListener<Group> {

    private Command command;

    public InviteToGroupListener(Command command) {
        this.command = command;
    }

    @Override
    public void onData(SocketIOClient sioc, Group t,
                       AckRequest ar) throws Exception {
        String username = sioc.getHandshakeData().getSingleUrlParam("username");
        System.out.println("inviteToGroup: [" + username + "]->" + t.toString());
        ObjectNode result = command.ok();

        try {
            if (command.getDb().canInviteUsers(username, t.getRoom())) {
                System.out.println("inviteToGroup: [" + username + "] can invite to group");
                if (t.getUsers() != null && !t.getUsers().isEmpty()) {
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

                        for (User groupUser : group.getUsers()) {
                            if (!groupUser.getUsername().equalsIgnoreCase(username)) {
                                co = new ChatObject();
                                co.setBody(currentUser.getNickname() + " пригласил " + user.getNickname());
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
                    result = command.error(400, "inviteToGroup: [" + username + "]<-not provided users array");
                }
            } else {
                System.out.println("inviteToGroup: [" + username + "] can't invite to group");
                result = command.error(403, "inviteToGroup: [" + username + "]<- can't invite to group");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result = command.error(500, "inviteToGroup: [" + username + "]<-" + e.getMessage());
        }

        System.out.println("inviteToGroup: [" + username + "]<-" + result.toString());
        ar.sendAckData(result.toString());

    }
}
