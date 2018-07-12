package kz.dnd.blchat.listeners.group;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kz.dnd.blchat.common.ChatObject;
import kz.dnd.blchat.common.Group;
import kz.dnd.blchat.common.User;
import kz.dnd.blchat.wrappers.Command;

public class RemoveFromGroupListener implements DataListener<Group> {

    private Command command;

    public RemoveFromGroupListener(Command command) {
        this.command = command;
    }

    @Override
    public void onData(SocketIOClient sioc, Group t,
                       AckRequest ar) throws Exception {
        String username = sioc.getHandshakeData().getSingleUrlParam("username");
        System.out.println("removeFromGroup: [" + username + "]->" + t.toString());
        ObjectNode result = command.ok();

        try {
            if (command.getDb().canInviteUsers(username, t.getRoom())) {
                System.out.println("removeFromGroup: [" + username + "] can invite to group");
                if (t.getUsers() != null && !t.getUsers().isEmpty()) {
                    for (User user : t.getUsers()) {
                        System.out.println("removeFromGroup: [" + username + "] inviting user: " + user.getUsername());

                        command.getDb().leaveGroup(username, t.getRoom(), user.getUsername());

                        ChatObject co = new ChatObject();
                        co.setBody("Вас удалили из группы");
                        co.setFrom(username);
                        co.setMimetype("service");
                        co.setTo(user.getUsername());
                        co.setEvent("service");
                        co.setRoom(t.getRoom());
                        co.setTimestamp("" + System.currentTimeMillis());
                        command.sendMessage(co, command.getServer(), sioc, command.getDb());
                    }
                } else {
                    result = command.error(400, "removeFromGroup: [" + username + "]<-users list is empty");
                }
            } else {
                System.out.println("removeFromGroup: [" + username + "] can't kick to group");
                result = command.error(403, "removeFromGroup: [" + username + "]<-can't kick from group: " + t.getRoom());
            }

        } catch (Exception e) {
            e.printStackTrace();
            result = command.error(500, "removeFromGroup: [" + username + "]<-" + e.getMessage());
        }

        System.out.println("removeFromGroup: [" + username + "]<-" + result.toString());
        ar.sendAckData(result.toString());

    }
}
