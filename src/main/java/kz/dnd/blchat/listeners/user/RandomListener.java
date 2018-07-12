package kz.dnd.blchat.listeners.user;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kz.dnd.blchat.common.User;
import kz.dnd.blchat.wrappers.Command;

public class RandomListener implements DataListener<User> {

    private Command command;

    public RandomListener(Command command) {
        this.command = command;
    }

    @Override
    public void onData(SocketIOClient sioc, User t,
                       AckRequest ar) throws Exception {
        String username = sioc.getHandshakeData().getSingleUrlParam("username");
        System.out.println("random: [" + username + "]->" + t.toString());
        ObjectNode result = command.ok();

        String resultUsername = "";

        try {
            for (String key : command.online.keySet()) {
                if (command.getDb().isRoster(username, key, "r-" + username + "-" + key) == -1
                        && !key.equalsIgnoreCase(username)) {
                    resultUsername = key;
                    break;
                }
            }

            if (resultUsername.isEmpty()) {
                resultUsername = command.getDb().getRandom(username);
            }

            if (resultUsername.isEmpty()) {
                result = command.error(404, "random: [" + username + "]<-not found user for him");
            } else {
                result = command.ok().put("username", resultUsername);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = command.error(500, "random: [" + username + "]<-" + e.getMessage());
        }

        System.out.println("random: [" + username + "]<-" + result.toString());
        ar.sendAckData(result.toString());
    }
}
