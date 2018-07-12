package kz.dnd.blchat.listeners;

import com.corundumstudio.socketio.AuthorizationListener;
import com.corundumstudio.socketio.HandshakeData;
import kz.dnd.blchat.common.DB;
import kz.dnd.blchat.wrappers.Command;

public class AuthorizationsListener implements AuthorizationListener {

    private DB db;

    public AuthorizationsListener(DB db) {
        this.db = db;
    }

    @Override
    public boolean isAuthorized(HandshakeData data) {
        String username = data.getSingleUrlParam("username");
        String password = data.getSingleUrlParam("password");

        if (username.equalsIgnoreCase(Command.GUEST_USERNAME)) {
            return true;
        } else {
            boolean auth = false;

            try {
                auth = db.authUser(username, password);

                System.out.println(username + " p: " + password + " auth: " + auth);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return auth;
        }
    }

}
