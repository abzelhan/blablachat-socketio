package kz.dnd.blchat.listeners.group;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kz.dnd.blchat.common.Group;
import kz.dnd.blchat.wrappers.Command;

import java.util.List;

public class PublicGroupsListener implements DataListener<Group> {

    private Command command;

    public PublicGroupsListener(Command command) {
        this.command = command;
    }

    @Override
    public void onData(SocketIOClient sioc, Group t,
                       AckRequest ar) throws Exception {
        String username = sioc.getHandshakeData().getSingleUrlParam("username");
        System.out.println("publicGroups: [" + username + "]->" + t.toString());
        JsonNode result = command.ok();

        try {
            t.setOwner(username);

            System.out.println("publicGroups: [" + username + "]->" + "getting public groups");

            List<Group> groups = command.getDb().getPublicGroups(username);

            if (groups != null && !groups.isEmpty()) {
                System.out.println("publicGroups: [" + username + "]->" + "got " + groups.size() + " groups");

                ArrayNode arr = command.mapper.createArrayNode();
                for (Group group : groups) {
                    ObjectNode o = command.mapper.createObjectNode();
                    o.put("room", group.getUuid());
                    o.put("publicTitle", group.getPublicTitle());
                    o.put("usersCount", group.getParticipants());
                    o.put("isInMyList", group.isIsInMyList());

                    if (group.getImage() != null) {
                        ObjectNode i = command.mapper.createObjectNode();
                        i.put("code", group.getImage().getCode());
                        i.put("big", group.getImage().getBig());

                        o.set("image", i);
                    }

                    arr.add(o);
                }
                result = command.ok().putArray("groups").addAll(arr);
            } else {
                result = command.error(500, "publicGroups: [" + username + "]<-publicGroups are empty");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = command.error(500, "publicGroups: [" + username + "]<-" + e.getMessage());
        }

        System.out.println("publicGroups: [" + username + "]<-" + result.toString());
        ar.sendAckData(result);

    }
}
