package kz.dnd.blchat.listeners.group;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kz.dnd.blchat.common.ChatObject;
import kz.dnd.blchat.common.Group;
import kz.dnd.blchat.common.User;
import kz.dnd.blchat.wrappers.Command;

public class RemoveAdminListener implements DataListener<Group> {

    private Command command;

    public RemoveAdminListener(Command command) {
        this.command = command;
    }

    @Override
    public void onData(SocketIOClient sioc, Group t,
                       AckRequest ar) throws Exception {
        String username = sioc.getHandshakeData().getSingleUrlParam("username");
        System.out.println("removeAdmin: [" + username + "]->" + t.toString());
        ObjectNode result = command.ok();

        try {
            if (command.getDb().canInviteUsers(username, t.getRoom())) {
                System.out.println("removeAdmin: [" + username + "] can remove admin to group");
                if (t.getUsers() != null && !t.getUsers().isEmpty()) {
                    for (User user : t.getUsers()) {
                        System.out.println("removeAdmin: [" + username + "] removing user: " + user.getUsername());

                        command.getDb().removeAdmin(username, t.getRoom(), user.getUsername());

                        ChatObject co = new ChatObject();
                        co.setBody("Вы больше не администратор :(");
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
                                co.setBody(currentUser.getNickname() + " снял привилегии админа " + user.getNickname());
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
                    result = command.error(400, "removeAdmin: [" + username + "]<-users list is empty");
                }
            } else {
                System.out.println("removeAdmin: [" + username + "] can't assign admin to group");
                result = command.error(403, "removeAdmin: [" + username + "]<-can't remove admin from group: " + t.getRoom());
            }

        } catch (Exception e) {
            e.printStackTrace();
            result = command.error(500, "removeAdmin: [" + username + "]<-" + e.getMessage());
        }

        System.out.println("removeAdmin: [" + username + "]<-" + result.toString());
        ar.sendAckData(result.toString());

    }
}
