package kz.dnd.blchat.listeners.user;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kz.dnd.blchat.common.ChatObject;
import kz.dnd.blchat.common.User;
import kz.dnd.blchat.wrappers.Command;

import java.util.List;

public class SaveUserListener implements DataListener<User> {

    private Command command;

    public SaveUserListener(Command command) {
        this.command = command;
    }

    @Override
    public void onData(SocketIOClient sioc, User t,
                       AckRequest ar) throws Exception {
        String username = sioc.getHandshakeData().getSingleUrlParam("username");
        System.out.println("saveUser: [" + username + "]->" + t.toString());
        ObjectNode result = command.ok();

        try {
            if (t.getUsername().equalsIgnoreCase(username)) {
                User user = command.getDb().getUserByUsername(username);
                System.out.println("saveUser: [" + username + "]-> found user: " + user.getId());

                t.setId(user.getId());

                command.getDb().saveUser(t);

                User u = command.getDb().getUserByUsername(username);

                List<User> rosters = command.getDb().getRosters(username);

                for (User us : rosters) {
                    ChatObject c = new ChatObject();
                    c.setBody(u.getNickname() + " изменил информацию о себе");
                    c.setEnc(false);
                    c.setEvent("userUpdate");
                    c.setFrom(username);
                    c.setTo(us.getUsername());
                    c.setType("userUpdate");
                    c.setTimestamp("" + System.currentTimeMillis());

                    command.sendMessage(c, command.getServer(), sioc, command.getDb());
                }

            } else {
                result = command.error(403, "saveUser: [" + username + "]<- trying to save another user");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = command.error(500, "saveUser: [" + username + "]<-" + e.getMessage());
        }

        System.out.println("saveUser: [" + username + "]<-" + result.toString());
        ar.sendAckData(result.toString());
    }
}
