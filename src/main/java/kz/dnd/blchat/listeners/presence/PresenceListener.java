package kz.dnd.blchat.listeners.presence;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kz.dnd.blchat.common.ChatObject;
import kz.dnd.blchat.common.Presense;
import kz.dnd.blchat.common.User;
import kz.dnd.blchat.common.UserConnection;
import kz.dnd.blchat.wrappers.Command;

import java.util.List;

public class PresenceListener implements DataListener<Presense> {

   
    private Command command;


    public PresenceListener(Command command) {
        this.command = command;
    }

    @Override
    public void onData(SocketIOClient sioc, Presense t, AckRequest ar) throws Exception {

        String username = sioc.getHandshakeData().getSingleUrlParam("username");
        System.out.println("presense: [" + username + "]->" + t.toString());
        ObjectNode result = command.ok();

        try {
            List<User> rosters = command.getDb().getRosters(username);
            System.out.println("username[" + username + "] got: " + rosters.size() + " rosters");

            User current = new User();
            current.setUsername(username);
            current.setLastActivity(t.getLastActivity());

            boolean isOnlinePresence = t.getLastActivity() == 0;

            try {
                UserConnection uc = command.online.get(username).get(0);
                uc.setIsAway(!isOnlinePresence);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (isOnlinePresence) {
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

            }

                    /*if (isOnlinePresence) {
                        awayUsers.remove(username);
                    } else {
                        if (!awayUsers.contains(username)) {
                            awayUsers.add(username);
                        }
                    }*/
            for (User roster : rosters) {
                boolean isOn = command.online.containsKey(roster.getUsername());
                //System.out.println("username[" + username + "] roster: " + roster.getUsername() + " isOn: " + isOn);

                if (isOn) {
                    //this is for the roster
                    try {
                        List<UserConnection> list = command.online.get(roster.getUsername());
                        if (!list.isEmpty()) {
                            SocketIOClient s = command.getServer().getClient(list.get(0).getUuid());
                            if (s != null) {
                                command.sendPresense(current, command.getServer(), s, isOnlinePresence);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            result = command.error(500, "presense: [" + username + "]<-" + e.getMessage());
        }

        System.out.println("presense: [" + username + "]<-" + result.toString());
        ar.sendAckData(result.toString());

    }
}
