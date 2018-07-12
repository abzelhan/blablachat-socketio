package kz.dnd.blchat.listeners;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DisconnectListener;
import kz.dnd.blchat.common.User;
import kz.dnd.blchat.common.UserConnection;
import kz.dnd.blchat.wrappers.Command;

import java.util.ArrayList;
import java.util.List;

public class DisconectionListener implements DisconnectListener {

    private Command command;

    public DisconectionListener(Command command) {
        this.command = command;
    }

    @Override
    public void onDisconnect(SocketIOClient sioc) {

        String username = sioc.getHandshakeData().getSingleUrlParam("username");
        System.out.println("<==username [" + username + "] disconnected");

                    /*List<UUID> list = online.get(username);
            list.remove(sioc.getSessionId());

            online.put(username, list);*/
        try {
            command.getDb().setLastActivity(username);
        } catch (Exception e) {
            e.printStackTrace();
        }

        command.online.put(username, new ArrayList<>());

        try {
            List<User> rosters = command.getDb().getRosters(username);
            System.out.println("username[" + username + "] got: " + rosters.size() + " rosters");

            User current = new User();
            current.setUsername(username);
            current.setLastActivity(10 * 1000);

            for (User roster : rosters) {
                boolean isOn = command.online.containsKey(roster.getUsername());
                //System.out.println("username[" + username + "] roster: " + roster.getUsername() + " isOn: " + isOn);

                if (isOn) {
                    //this is for the roster
                    try {
                        UserConnection uc = command.online.get(roster.getUsername()).get(0);
                        SocketIOClient s = command.getServer().getClient(uc.getUuid());
                        if (s != null && !uc.isIsAway()) {
                            command.sendPresense(current, command.getServer(), s, false);
                        }
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
