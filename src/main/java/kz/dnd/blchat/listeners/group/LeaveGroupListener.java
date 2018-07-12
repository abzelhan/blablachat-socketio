package kz.dnd.blchat.listeners.group;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kz.dnd.blchat.common.ChatObject;
import kz.dnd.blchat.common.Group;
import kz.dnd.blchat.common.User;
import kz.dnd.blchat.wrappers.Command;

public class LeaveGroupListener implements DataListener<Group> {

    private Command command;

    public LeaveGroupListener(Command command) {
        this.command = command;
    }

    @Override
    public void onData(SocketIOClient sioc, Group t,
                       AckRequest ar) throws Exception {
        String username = sioc.getHandshakeData().getSingleUrlParam("username");
        System.out.println("leaveGroup: [" + username + "]->" + t.toString());
        ObjectNode result = command.ok();

        try {
            System.out.println("leaveGroup: [" + username + "] leaving group: " + t.getRoom());

            command.getDb().leaveGroup(username, t.getRoom(), username);

            ChatObject co = new ChatObject();
            co.setBody("Вы покинули группу :(");
            co.setFrom(username);
            co.setMimetype("service");
            co.setTo(username);
            co.setEvent("service");
            co.setRoom(t.getRoom());
            co.setTimestamp("" + System.currentTimeMillis());
            command.sendMessage(co, command.getServer(), sioc, command.getDb());

            Group group = command.getDb().getGroup(t);
            User currentUser = command.getDb().getUserByUsername(username);

            for (User groupUser : group.getUsers()) {
                if (!groupUser.getUsername().equalsIgnoreCase(username)) {
                    co = new ChatObject();
                    co.setBody(currentUser.getNickname() + " покинул группу ");
                    co.setFrom(username);
                    co.setMimetype("service");
                    co.setTo(groupUser.getUsername());
                    co.setEvent("service");
                    co.setRoom(t.getRoom());
                    co.setTimestamp("" + System.currentTimeMillis());
                    command.sendMessage(co, command.getServer(), sioc, command.getDb());

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            result = command.error(500, "leaveGroup: [" + username + "]<-" + e.getMessage());
        }

        System.out.println("leaveGroup: [" + username + "]<-" + result.toString());
        ar.sendAckData(result.toString());

    }
}
