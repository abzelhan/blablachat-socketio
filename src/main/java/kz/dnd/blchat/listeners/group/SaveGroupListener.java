package kz.dnd.blchat.listeners.group;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kz.dnd.blchat.common.ChatObject;
import kz.dnd.blchat.common.Group;
import kz.dnd.blchat.common.User;
import kz.dnd.blchat.wrappers.Command;

public class SaveGroupListener implements DataListener<Group> {

    private Command command;

    public SaveGroupListener(Command command) {
        this.command = command;
    }

    @Override
    public void onData(SocketIOClient sioc, Group t,
                       AckRequest ar) throws Exception {
        String username = sioc.getHandshakeData().getSingleUrlParam("username");
        System.out.println("saveGroup: [" + username + "]->" + t.toString());
        ObjectNode result = command.ok();

        try {
            if (command.getDb().canInviteUsers(username, t.getUuid())) {
                command.getDb().saveGroup(t);

                if (t.getRoom() == null || t.getRoom().isEmpty()) {
                    t.setRoom(t.getUuid());
                }

                Group g = command.getDb().getGroup(t, false);

                User changer = command.getDb().getUserByUsername(username);

                for (User user : g.getUsers()) {
                    ChatObject c = new ChatObject();
                    c.setBody(changer.getNickname() + " изменил информацию о группе");
                    c.setEnc(false);
                    c.setEvent("groupUpdate");
                    c.setFrom(g.getRoom());
                    c.setTo(user.getUsername());
                    c.setType("groupUpdate");
                    c.setTimestamp("" + System.currentTimeMillis());

                    command.sendMessage(c, command.getServer(), sioc, command.getDb());

                    c.setEvent("service");
                    c.setType("service");
                    c.setRoom(g.getRoom());
                    c.setMimetype("service");

                    command.sendMessage(c, command.getServer(), sioc, command.getDb());
                }
            } else {
                result = command.error(403, "saveGroup: [" + username + "]<-trying to change another group");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = command.error(500, "saveGroup: [" + username + "]<-" + e.getMessage());
        }

        System.out.println("saveGroup: [" + username + "]<-" + result.toString());
        ar.sendAckData(result.toString());
    }
}
