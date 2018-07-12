package kz.dnd.blchat.listeners;

import com.corundumstudio.socketio.AckCallback;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.ConnectListener;
import kz.dnd.blchat.common.ChatObject;
import kz.dnd.blchat.common.User;
import kz.dnd.blchat.common.UserConnection;
import kz.dnd.blchat.wrappers.Command;

import java.util.ArrayList;
import java.util.List;

public class ConnectionListener implements ConnectListener {

    private Command command;

    public ConnectionListener(Command command) {
        this.command = command;
    }

    @Override
    public void onConnect(SocketIOClient sioc) {

        String username = sioc.getHandshakeData().getSingleUrlParam("username");
        String v = sioc.getHandshakeData().getSingleUrlParam("v");

        System.out.println("-->username [" + username + "] connected");

        if (!username.equalsIgnoreCase(command.GUEST_USERNAME)) {
            List<UserConnection> users = command.online.get(username);

            if (users != null && !users.isEmpty()) {
                for (UserConnection user : users) {
                    SocketIOClient client = command.getServer().getClient(user.getUuid());
                    client.sendEvent("kick", new AckCallback<String>(String.class) {
                        @Override
                        public void onSuccess(String result) {
                            System.out.println(user.toString() + " kicked: " + result);
                        }
                    }, "anotherConnect");

                    client.disconnect();
                }
            }

            users = new ArrayList<>();

            users.add(new UserConnection(sioc.getSessionId(), false, v));

            command.online.put(username, users);
            //sending offline messages
            try {
                List<ChatObject> offlineMessages = command.getDb().getOfflineMessages(username);

                if (offlineMessages != null && !offlineMessages.isEmpty()) {
                    offlineMessages.forEach((co) -> {
                        try {
                            command.sendMessage(co, command.getServer(), sioc, command.getDb());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //sending presense info
            try {
                List<User> rosters = command.getDb().getRosters(username);
                System.out.println("username[" + username + "] got: " + rosters.size() + " rosters");

                User current = new User();
                current.setUsername(username);
                current.setLastActivity(0);

                for (User roster : rosters) {
                    List<UserConnection> u = command.online.get(roster.getUsername());

                    boolean isOn = u != null
                            && !u.isEmpty()
                            && !u.get(0).isIsAway();

                    //System.out.println("username[" + username + "] roster: " + roster.getUsername() + " isOn: " + isOn);
                    //this is for connected user
                    command.sendPresense(roster, command.getServer(), sioc, isOn);

                    if (isOn) {
                        //this is for the roster
                        try {
                            UserConnection uc = command.online.get(roster.getUsername()).get(0);
                            SocketIOClient s = command.getServer().getClient(uc.getUuid());
                            if (s != null && !uc.isIsAway()) {
                                command.sendPresense(current, command.getServer(), s, true);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
