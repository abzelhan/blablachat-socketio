package kz.dnd.blchat.listeners.chatobject;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kz.dnd.blchat.common.ChatObject;
import kz.dnd.blchat.wrappers.Command;

public class ViewedListener implements DataListener<ChatObject> {

   private Command command;

    public ViewedListener(Command command) {
        this.command = command;
    }

    @Override
    public void onData(SocketIOClient sioc, ChatObject data,
                       AckRequest ar) throws Exception {

        String username = sioc.getHandshakeData().getSingleUrlParam("username");
        System.out.println("viewed: [" + username + "]->" + data.toString());
        ObjectNode result = command.ok();

        try {

            if (username.equalsIgnoreCase(command.GUEST_USERNAME)) {
                result = command.error(401, "viewed: [" + username + "]<-guest trying to view");
            } else {
                System.out.println("viewed: [" + username + "]->" + "message: " + username + " to: " + data.getTo() + " message: " + data.getBody());

                //Sending got ACK
                result = command.ok().put("id", data.getId());

                //Seding delivered ACK to sender
                command.sendMessage(data.applyEvent("viewed"), command.getServer(), sioc, command.getDb());
            }

        } catch (Exception e) {
            e.printStackTrace();
            result = command.error(500, "viewed: [" + username + "]<-" + e.getMessage());
        }

        System.out.println("viewed: [" + username + "]<-" + result.toString());
        ar.sendAckData(result.toString());
    }
}
