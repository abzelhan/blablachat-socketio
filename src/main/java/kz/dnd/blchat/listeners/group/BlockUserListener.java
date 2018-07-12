package kz.dnd.blchat.listeners.group;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kz.dnd.blchat.common.Group;
import kz.dnd.blchat.common.User;
import kz.dnd.blchat.wrappers.Command;

import java.util.List;

public class BlockUserListener implements DataListener<Group> {

    private Command command;

    public BlockUserListener(Command command) {
        this.command = command;
    }

    @Override
    public void onData(SocketIOClient sioc, Group t,
                       AckRequest ar) throws Exception {
        String username = sioc.getHandshakeData().getSingleUrlParam("username");
        System.out.println("blockUser: [" + username + "]->" + t.toString());
        ObjectNode result = command.ok();

        try {
            List<User> users = t.getUsers();
            for (User user : users) {
                System.out.println("trying to block: " + user.getUsername());

                command.getDb().blockRoster(username, user.getUsername(), t.getRoom());
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = command.error(500, "blockUser: [" + username + "]<-" + e.getMessage());
        }

        System.out.println("blockUser: [" + username + "]<-" + result.toString());
        ar.sendAckData(result.toString());

    }
}
