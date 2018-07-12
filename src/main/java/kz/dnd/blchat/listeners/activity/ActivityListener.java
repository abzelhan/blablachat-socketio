package kz.dnd.blchat.listeners.activity;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kz.dnd.blchat.common.Activity;
import kz.dnd.blchat.common.Group;
import kz.dnd.blchat.common.User;
import kz.dnd.blchat.wrappers.Command;

public class ActivityListener implements DataListener<Activity> {

   private Command command;

    public ActivityListener(Command command) {
        this.command = command;
    }

    @Override
    public void onData(SocketIOClient sioc, Activity t, AckRequest ar) throws Exception {

        String username = sioc.getHandshakeData().getSingleUrlParam("username");
        System.out.println("activity: [" + username + "]->" + t.toString());
        ObjectNode result = command.ok();

        try {
            if (command.isPrivateRoom(t.getRoom())) {
                User roster = new User(t.getTo());
                System.out.println("activity: sending to " + t.getTo());
                command.sendActivity(roster, command.getServer(), t);
            } else {
                Group group = new Group(t.getRoom());
                group = command.getDb().getGroup(group);
                for (User roster : group.getUsers()) {
                    System.out.println("activity: sending to " + roster.getUsername());
                    command.sendActivity(roster, command.getServer(), t);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = command.error(500, "activity: [" + username + "]<-" + e.getMessage());
        }

        System.out.println("activity: [" + username + "]<-" + result.toString());
        ar.sendAckData(result.toString());
    }
}
