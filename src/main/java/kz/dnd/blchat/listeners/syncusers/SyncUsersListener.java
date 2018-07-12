package kz.dnd.blchat.listeners.syncusers;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kz.dnd.blchat.common.SyncUsers;
import kz.dnd.blchat.common.User;
import kz.dnd.blchat.wrappers.Command;

import java.util.List;

public class SyncUsersListener implements DataListener<SyncUsers> {

    private Command command;

    public SyncUsersListener(Command command) {
        this.command = command;
    }

    @Override
    public void onData(SocketIOClient sioc, SyncUsers t,
                       AckRequest ar) throws Exception {
        String username = sioc.getHandshakeData().getSingleUrlParam("username");
        System.out.println("syncUsers: [" + username + "]->" + t.toString());
        JsonNode result = command.ok();

        try {

            List<User> users = command.getDb().syncUsers(t.getPhones());

            System.out.println("syncUsers: [" + username + "]->" + "syncing users: " + t.getPhones().size() + " result: " + users.size());

            if (users != null && !users.isEmpty()) {
                ArrayNode arr = command.mapper.createArrayNode();
                for (User user : users) {
                    ObjectNode o = command.mapper.createObjectNode();
                    o.put("username", user.getUsername());
                    o.put("nickname", user.getNickname());
                    o.put("phone", user.getSearchPhone());

                    if (user.getImage() != null) {
                        ObjectNode i = command.mapper.createObjectNode();
                        i.put("code", user.getImage().getCode());
                        i.put("big", user.getImage().getBig());

                        o.set("image", i);
                    }
                    arr.add(o);
                }
                result = command.ok().putArray("users").addAll(arr);
            }

        } catch (Exception e) {
            e.printStackTrace();
            result = command.error(500, "syncUsers: [" + username + "]<-" + e.getMessage());
        }

        System.out.println("syncUsers: [" + username + "]<-" + result.toString());
        ar.sendAckData(result);

    }
}
