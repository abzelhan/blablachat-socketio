package kz.dnd.blchat.listeners.devicetoken;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kz.dnd.blchat.common.DeviceToken;
import kz.dnd.blchat.wrappers.Command;

public class RemoveDeviceListener implements DataListener<DeviceToken> {

   private Command command;

    public RemoveDeviceListener(Command command) {
        this.command = command;
    }

    @Override
    public void onData(SocketIOClient sioc, DeviceToken t, AckRequest ar) throws Exception {

        String username = sioc.getHandshakeData().getSingleUrlParam("username");
        System.out.println("removeDevice: [" + username + "]->" + t.toString());
        ObjectNode result = command.ok();

        try {
            t.setUsername(username);
            command.getDb().removeDeviceToken(t);
            ar.sendAckData(command.ok().toString());
        } catch (Exception e) {
            e.printStackTrace();
            result = command.error(500, "removeDevice: [" + username + "]<-" + e.getMessage());
        }

        System.out.println("removeDevice: [" + username + "]<-" + result.toString());
        ar.sendAckData(result.toString());
    }
}
