package kz.dnd.blchat.listeners.group;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kz.dnd.blchat.common.Group;
import kz.dnd.blchat.wrappers.Command;

public class GroupListener implements DataListener<Group> {

    private Command command;

    public GroupListener(Command command) {
        this.command = command;
    }

    @Override
    public void onData(SocketIOClient sioc, Group t,
                       AckRequest ar) throws Exception {
        String username = sioc.getHandshakeData().getSingleUrlParam("username");
        System.out.println("group: [" + username + "]->" + t.toString());
        ObjectNode result = command.ok();

        try {
            Group group = command.getDb().getGroup(t, false);

            if (group != null) {
                result.set("group", group.getJson());
            } else {
                result = command.error(500, "group: [" + username + "]<-group is null on " + t.getRoom());
            }

        } catch (Exception e) {
            e.printStackTrace();
            result = command.error(500, "group: [" + username + "]<-" + e.getMessage());
        }

        System.out.println("group: [" + username + "]<-" + result.toString());
        ar.sendAckData(result.toString());

    }
}
