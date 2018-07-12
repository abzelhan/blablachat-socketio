package kz.dnd.blchat.listeners.user;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kz.dnd.blchat.common.User;
import kz.dnd.blchat.wrappers.Command;

public class CheckNicknameListener implements DataListener<User> {

    private Command command;

    public CheckNicknameListener(Command command) {
        this.command = command;
    }

    @Override
    public void onData (SocketIOClient sioc, User t,
                 AckRequest ar) throws Exception {
        String username = sioc.getHandshakeData().getSingleUrlParam("username");
        System.out.println("checkNickname: [" + username + "]->" + t.toString());
        ObjectNode result = command.ok();

        try {
            boolean auth = command.getDb().checkNickname(t.getNickname());
            System.out.println("checkNickname: [" + username + "]->" + "check username: " + t.getNickname() + " result: " + auth);
            result = auth ? command.error(201, "checkNickname: [" + username + "]<- nick " + t.getNickname() + " is taken") : command.ok();
        } catch (Exception e) {
            e.printStackTrace();
            result = command.error(500, "checkNickname: [" + username + "]<-" + e.getMessage());
        }

        System.out.println("checkNickname: [" + username + "]<-" + result.toString());
        ar.sendAckData(result.toString());
    }
}
