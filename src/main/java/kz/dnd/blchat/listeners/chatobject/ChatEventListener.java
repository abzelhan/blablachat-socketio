package kz.dnd.blchat.listeners.chatobject;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kz.dnd.blchat.common.*;
import kz.dnd.blchat.wrappers.Command;

public class ChatEventListener implements DataListener<ChatObject> {

    private Command command;

    public ChatEventListener(Command command) {
        this.command = command;
    }

    @Override
    public void onData(SocketIOClient client, ChatObject data, AckRequest ackRequest) throws Exception {

        String username = client.getHandshakeData().getSingleUrlParam("username");
        System.out.println("chatevent: [" + username + "]->" + data.toString());
        ObjectNode result = command.ok();

        try {
            //security reasons
            data.setFrom(username);

            if (username.equalsIgnoreCase(command.GUEST_USERNAME)) {
                System.out.println("chatevent: [" + username + "]->" + "unauthorized sending message: " + username + " to[" + data.getRoom() + "]: " + data.getTo() + " message: " + data.getBody());
                ackRequest.sendAckData(command.error(401, "chatevent: [" + username + "]<-unauthorized sending message").toString());
            } else {
                System.out.println("chatevent: [" + username + "]->" + "from: " + username + " to: " + data.getTo() + " message: " + data.getBody());

                int newRoster = command.getDb().addRoster(data.getFrom(), data.getTo(), data.getRoom());

                if (newRoster < 2) {
                    if (newRoster == -1) {
                        String message = "Пользователь начал приватный чат с вами из своего списка контактов. Ваш собеседник знает кто вы.";

                        if (data.getRoom().startsWith("r-")) {
                            message = "Пользователь начал анонимный чат с вами из случайного поиска. Ваш собеседник не знает кто вы.";
                        } else if (!data.getRoom().startsWith("p-")) {
                            message = "Вы были добавлены в группу. Создатель группы знает кто вы.";
                        }

                        ChatObject co = new ChatObject();
                        co.setBody(message);
                        co.setTo(data.getTo());
                        co.setRoom(data.getRoom());
                        co.setFrom("command.getServer()");
                        co.setTimestamp("" + System.currentTimeMillis());
                        co.setType("service");
                        co.setMimetype("service");
                        co.setEvent("service");
                        co.setId("" + System.currentTimeMillis());

                        command.sendMessage(co, command.getServer(), client, command.getDb());
                    }

                    //Sending sent ACK
                    result = command.ok().put("id", data.getId());

                    //Activity that user finished typing
                    Activity activity = new Activity();
                    activity.setRoom(data.getRoom());
                    activity.setTo(data.getTo());
                    activity.setType("typing");
                    activity.setFrom(username);
                    activity.setActive(false);

                    data.setTimestamp("" + System.currentTimeMillis());

                    if (command.isPrivateRoom(data.getRoom())) {
                        command.sendMessage(data, command.getServer(), client, command.getDb());
                        command.sendActivity(new User(data.getTo()), command.getServer(), activity);
                    } else {
                        Group group = new Group();
                        group.setRoom(data.getRoom());

                        group = command.getDb().getGroup(group);

                        boolean shouldInvite = true;

                        for (User user : group.getUsers()) {
                            if (user.getUsername().equalsIgnoreCase(username)) {
                                shouldInvite = false;
                            } else {
                                data.setTo(user.getUsername());
                                command.sendMessage(data, command.getServer(), client, command.getDb());
                                command.sendActivity(user, command.getServer(), activity);
                            }
                        }

                        if (shouldInvite) {
                            command.getDb().inviteUser(username, group.getRoom(), username);
                        }
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            result = command.error(500, "chatevent: [" + username + "]<-" + e.getMessage());
        }

        System.out.println("chatevent: " + username + "]<-" + result.toString());
        ackRequest.sendAckData(result.toString());
    }
}
