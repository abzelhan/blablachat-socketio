package kz.dnd.blchat.listeners.user;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kz.dnd.blchat.common.User;
import kz.dnd.blchat.wrappers.Command;

public class UserListener implements DataListener<User> {


    private Command command;

    public UserListener(Command command) {
        this.command = command;
    }

    @Override
    public void onData(SocketIOClient sioc, User t,
                       AckRequest ar) throws Exception {
        String username = sioc.getHandshakeData().getSingleUrlParam("username");
        System.out.println("user: [" + username + "]->" + t.toString());
        ObjectNode result = command.ok();

        try {
            User user = command.getDb().getUserByUsername(t.getUsername());

            if (user != null) {
                result.set("user", user.getJson());
            } else {
                result = command.error(404, "user: [" + username + "]<-user not found: " + t.getUsername());
            }

        } catch (Exception e) {
            e.printStackTrace();
            result = command.error(500, "user: [" + username + "]<-" + e.getMessage());
        }

        System.out.println("user: [" + username + "]<-" + result.toString());
        ar.sendAckData(result.toString());

    }
}
