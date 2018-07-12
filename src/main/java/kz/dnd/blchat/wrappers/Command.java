package kz.dnd.blchat.wrappers;

import com.corundumstudio.socketio.AckCallback;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.dnd.slack.SlackAttachment;
import com.dnd.slack.SlackWebhook;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kz.dnd.blchat.common.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Command {

    //final static List<String> awayUsers = new ArrayList<>();
    public final static String GUEST_USERNAME = "guest";
    public final static Map<String, List<UserConnection>> online = new HashMap<>();
    public final static ObjectMapper mapper = new ObjectMapper();

    private SocketIOServer server;
    private DB db;

    public Command(SocketIOServer server, DB db) {
        this.server = server;
        this.db = db;
    }

    public static void sendMessage(ChatObject data, SocketIOServer server, SocketIOClient client, DB db) throws Exception {
        System.out.println("sendMessage[" + data.getEvent() + "]: from[" + data.getFrom() + "] - " + data.getId());

        List<UserConnection> toUUID;
        //boolean isAway = false;
        if (data.getEvent().equalsIgnoreCase("chatevent")
                || data.getEvent().equalsIgnoreCase("invite")
                || data.getEvent().equalsIgnoreCase("service")
                || data.getEvent().equalsIgnoreCase("groupUpdate")
                || data.getEvent().equalsIgnoreCase("userUpdate")) {
            String to = data.getTo();
            toUUID = online.get(to);
            System.out.println("sendMessage[" + data.getEvent() + "] - " + data.getId() + ": to " + to);
        } else {
            System.out.println("sendMessage[" + data.getEvent() + "] - " + data.getId() + ": to " + data.getFrom());
            toUUID = online.get(data.getFrom());
        }

        if (toUUID != null && !toUUID.isEmpty()) {
            for (UserConnection to : toUUID) {
                if (!to.isIsAway()) {
                    SocketIOClient clientIO = server.getClient(to.getUuid());
                    System.out.println("sendMessage[" + data.getEvent() + "] - " + data.getId() + " sending to [" + to + "] - " + clientIO + " v: " + to.getV());

                    if (!data.isEnc() && !to.getV().equalsIgnoreCase("0.1")
                            && (data.getEvent().equalsIgnoreCase("chatevent")
                            || data.getEvent().equalsIgnoreCase("service"))) {

                        System.out.println("sendMessage[" + data.getEvent() + "] - " + data.getId() + "encrypting message to [" + to + "] -  v: " + to.getV());
                        data.setBody(java.util.Base64.getEncoder().encodeToString(data.getBody().getBytes()));
                        data.setEnc(true);
                    }

                    //if (clientIO.isChannelOpen()) {
                    clientIO.sendEvent(data.getEvent(), new AckCallback<String>(String.class
                    ) {
                        @Override
                        public void onSuccess(String result) {
                            System.out.println("sendMessage[" + data.getEvent() + "] - ack from client: " + toUUID.toString() + " data: " + result);

                            if (!data.getEvent().equalsIgnoreCase("delivered") && !data.getEvent().equalsIgnoreCase("service")) {
                                //Seding delivered ACK to sender
                                try {
                                    sendMessage(data.applyEvent("delivered"), server, client, db);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onTimeout() {
                            super.onTimeout(); //To change body of generated methods, choose Tools | Templates.

                            try {
                                System.out.println("sendMessage[" + data.getEvent() + "] - timeout from client: " + toUUID.toString() + ". Reason: timeout");
                                //if (!data.getEvent().equalsIgnoreCase("invite")) {
                                db.archive(data, data.getEvent().equalsIgnoreCase("chatevent"), true);
                                //}
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    }, data);
                } else {
                    //user is away
                    System.out.println("sendMessage[" + data.getEvent() + "] - this should be send via push. Reason: userIsAway");
                    if (!data.getEvent().equalsIgnoreCase("userUpdate")){
                        db.archive(data, data.getEvent().equalsIgnoreCase("chatevent"), true);
                    }
                }
                /*} else {
                    System.out.println("sendMessage[" + data.getEvent() + "] - this should be send via push. Reason: channelClose");
                    db.archive(data, data.getEvent().equalsIgnoreCase("chatevent"), true);
                }*/
            }
        } else {
            System.out.println("sendMessage[" + data.getEvent() + "] - this should be send via push. Reason: uuid is empty");
            //if (!data.getEvent().equalsIgnoreCase("invite")) {
            if (!data.getEvent().equalsIgnoreCase("userUpdate")){
                db.archive(data, data.getEvent().equalsIgnoreCase("chatevent"), true);
            }
            //}
            //TODO: push message
        }
    }


    public static void sendPresense(User roster, SocketIOServer server, SocketIOClient sioc, boolean isOn) {
        Presense p = new Presense();
        p.setUsername(roster.getUsername());

        if (isOn) {
            p.setLastActivity(0);
        } else {
            long lastActivity = roster.getLastActivity();
            if (lastActivity <= 10000) {
                p.setLastActivity(10);
            } else {
                p.setLastActivity((System.currentTimeMillis() - lastActivity) / 1000);
            }
        }

        sioc.sendEvent("presence", new AckCallback<String>(String.class
        ) {
            @Override
            public void onSuccess(String result) {
            }

            @Override
            public void onTimeout() {
                super.onTimeout(); //To change body of generated methods, choose Tools | Templates.
            }

        }, p);
    }

    public static void sendActivity(User roster, SocketIOServer server, Activity activity) {

        if (online.containsKey(roster.getUsername())) {
            List<UserConnection> sockets = online.get(roster.getUsername());
            if (!sockets.isEmpty()) {
                UserConnection uc = sockets.get(0);
                if (!uc.isIsAway()) {
                    SocketIOClient sioc = server.getClient(uc.getUuid());

                    sioc.sendEvent("activity", new AckCallback<String>(String.class) {
                        @Override
                        public void onSuccess(String result) {
                        }

                        @Override
                        public void onTimeout() {
                            super.onTimeout(); //To change body of generated methods, choose Tools | Templates.
                        }

                    }, activity);
                }
            }
        }

    }

    public static boolean isPrivateRoom(String room) {
        return room.startsWith("p-") || room.startsWith("r-") || room.startsWith("o-");
    }

    public static ObjectNode getResult() {
        ObjectNode result = mapper.createObjectNode();
        return result;
    }

    public static ObjectNode ok() {
        return getResult().put("status", 200);
    }

    public static ObjectNode error(int code, String trace) {
        List<SlackAttachment> list = new ArrayList<>();

        SlackAttachment at = new SlackAttachment();
        at.setTitle("Trace");
        at.setText(trace);

        list.add(at);

        SlackWebhook.sendMessage("Error: " + code, list);

        return getResult().put("status", code);
    }

    public SocketIOServer getServer() {
        return server;
    }

    public void setServer(SocketIOServer server) {
        this.server = server;
    }

    public DB getDb() {
        return db;
    }

    public void setDb(DB db) {
        this.db = db;
    }
}
