package kz.dnd.blchat.listeners.user;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kz.dnd.blchat.common.User;
import kz.dnd.blchat.wrappers.Command;

public class SendInviteListener implements DataListener<User> {

    private Command command;

    public SendInviteListener(Command command) {
        this.command = command;
    }

    @Override
    public void onData(SocketIOClient sioc, User t,
                       AckRequest ar) throws Exception {
        String username = sioc.getHandshakeData().getSingleUrlParam("username");
        System.out.println("sendInvite: [" + username + "]->" + t.toString());
        ObjectNode result = command.ok();

        try {
            command.getDb().sendInvite(username, t.getPhone(), t.getLang());
        } catch (Exception e) {
            e.printStackTrace();
            result = command.error(500, "sendInvite: [" + username + "]<-" + e.getMessage());
        }

        System.out.println("sendInvite: [" + username + "]<-" + result.toString());
        ar.sendAckData(result.toString());
    }
}
