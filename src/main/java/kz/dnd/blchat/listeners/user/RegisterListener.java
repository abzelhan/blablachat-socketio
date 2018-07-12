package kz.dnd.blchat.listeners.user;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kz.dnd.blchat.common.User;
import kz.dnd.blchat.wrappers.Command;

public class RegisterListener implements DataListener<User> {

    private Command command;


    public RegisterListener(Command command) {
        this.command = command;
    }

    @Override
    public void onData(SocketIOClient sioc, User t,
                       AckRequest ar) throws Exception {
        String username = sioc.getHandshakeData().getSingleUrlParam("username");
        System.out.println("register: [" + username + "]->" + t.toString());
        ObjectNode result = command.ok();

        try {
            String phone = t.getPhone().replaceAll("\\+", "").replaceAll(" ", "");
            t.setPhone(phone);

            System.out.println("registering user: " + phone + " nickname: " + t.getNickname());

            User user = command.getDb().registerUser(t);

            System.out.println("registered user: " + user.getUsername() + " password: " + user.getPassword());

            ObjectNode image = command.mapper.createObjectNode();
            image.put("code", user.getImage().getCode());
            image.put("big", user.getImage().getBig());

            ArrayNode ig = command.mapper.createArrayNode();
            if (user.getInterestedGenders() != null) {
                for (String gender : user.getInterestedGenders()) {
                    ig.add(gender);
                }
            }

            if (user.getNickname() != null && !user.getNickname().isEmpty()) {
                result = command.ok().put("username", user.getUsername())
                        .put("password", user.getPassword())
                        .put("nickname", user.getNickname())
                        .put("gender", user.getGender());
                result.set("interestedGenders", ig);
            } else {
                result = command.error(201, "register: [" + username + "]<- new user: " + phone)
                        .put("username", user.getUsername())
                        .put("password", user.getPassword())
                        .put("gender", user.getGender());
                result.set("interestedGenders", ig);
            }
            result.set("image", image);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            result = command.error(500, "register: [" + username + "]<-" + e.getMessage());
        }

        System.out.println("register: [" + username + "]<-" + result.toString());
        ar.sendAckData(result.toString());
    }
}
