package kz.dnd.blchat.wrappers;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import kz.dnd.blchat.common.*;
import kz.dnd.blchat.listeners.AuthorizationsListener;
import kz.dnd.blchat.listeners.ConnectionListener;
import kz.dnd.blchat.listeners.DisconectionListener;
import kz.dnd.blchat.listeners.activity.ActivityListener;
import kz.dnd.blchat.listeners.chatobject.ChatEventListener;
import kz.dnd.blchat.listeners.chatobject.ViewedListener;
import kz.dnd.blchat.listeners.devicetoken.AddDeviceListener;
import kz.dnd.blchat.listeners.devicetoken.RemoveDeviceListener;
import kz.dnd.blchat.listeners.group.*;
import kz.dnd.blchat.listeners.presence.PresenceListener;
import kz.dnd.blchat.listeners.syncusers.SyncUsersListener;
import kz.dnd.blchat.listeners.user.*;


public class ServerWrapper {


    private SocketIOServer server;
    private DB db;
    private Command command;



    public ServerWrapper() {
    }

    public void initializeAndRunServer() throws InterruptedException {
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
        Configuration config = new Configuration();
        config.setPort(8080);
        config.getSocketConfig().setReuseAddress(true);

        System.out.println("reusing address");

        db = new DB("jdbc:mysql://localhost:3306/blabla?useUnicode=true&characterEncoding=utf8", "root", "root12");
        config.setAuthorizationListener(new AuthorizationsListener(db));

        server = new SocketIOServer(config);
        command = new Command(server, db);

        setListeners(server,command);

        boolean serverIsUp = false;
        while (!serverIsUp) {
            try {
                server.start();
                serverIsUp = true;
            } catch (Exception e) {
                System.out.println("port is not ready...waiting");
                Thread.sleep(1 * 1000);
            }
        }

        Thread closeChildThread = new Thread() {
            @Override
            public void run() {
                System.out.println("Stopping server");

                server.stop();
            }
        };

        Runtime.getRuntime().addShutdownHook(closeChildThread);

        System.out.println("server started - 12");

        /*try {
            String test = "Привет!";
            System.out.println("testing: " + test);
            for (byte theByte : test.getBytes()) {
                System.out.print(Integer.toHexString(theByte));
            }
            System.out.println("");

            String enc = new String(java.util.Base64.getEncoder().encode(test.getBytes()));

            System.out.println("base64: " + enc);

            byte[] dec = java.util.Base64.getDecoder().decode(enc);
            for (byte theByte : dec) {
                System.out.print(Integer.toHexString(theByte));
            }
            System.out.println("");
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        Thread.sleep(Integer.MAX_VALUE);

        server.stop();
    }

    public void setListeners(SocketIOServer server,Command command) {
        server.addConnectListener(new ConnectionListener(command));

        server.addDisconnectListener(new DisconectionListener(command));

        server.addEventListener(
                "presence", Presense.class,
                new PresenceListener(command)
        );

        server.addEventListener(
                "activity", Activity.class,
                new ActivityListener(command)
        );

        server.addEventListener(
                "chatevent", ChatObject.class,
                new ChatEventListener(command)
        );

        server.addEventListener(
                "viewed", ChatObject.class,
                new ViewedListener(command)
        );

        server.addEventListener(
                "addDevice", DeviceToken.class,
                new AddDeviceListener(command)
        );

        server.addEventListener(
                "removeDevice", DeviceToken.class,
                new RemoveDeviceListener(command)
        );

        server.addEventListener(
                "checkNickname", User.class,
                new CheckNicknameListener(command)
        );

        server.addEventListener(
                "saveUser", User.class,
                new SaveUserListener(command)
        );

        server.addEventListener(
                "saveGroup", Group.class,
                new SaveGroupListener(command)
        );

        server.addEventListener(
                "random", User.class,
                new RandomListener(command)
        );

        //Group chat
        server.addEventListener(
                "createGroup", Group.class,
                new CreateGroupListener( command)
        );

        server.addEventListener(
                "group", Group.class,
                new GroupListener(command)
        );

        server.addEventListener(
                "user", User.class,
                new UserListener(command)
        );

        server.addEventListener(
                "group_short", Group.class,
                new GroupShortListener(command)
        );

        server.addEventListener(
                "publicGroups", Group.class,
                new PublicGroupsListener(command)
        );

        server.addEventListener(
                "inviteToGroup", Group.class,
                new InviteToGroupListener(command)
        );

        server.addEventListener(
                "blockUser", Group.class,
                new BlockUserListener(command)
        );

        server.addEventListener(
                "assignAdmin", Group.class,
                new AssignAdminListener(command)
        );

        server.addEventListener(
                "removeAdmin", Group.class,
                new RemoveAdminListener(command)
        );

        server.addEventListener(
                "leaveGroup", Group.class,
                new LeaveGroupListener(command)
        );

        server.addEventListener(
                "removeFromGroup", Group.class,
                new RemoveFromGroupListener(command)
        );

        //User Functions
        server.addEventListener(
                "syncUsers", SyncUsers.class,
                new SyncUsersListener(command)
        );

        server.addEventListener(
                "sendInvite", User.class,
                new SendInviteListener(command)
        );

        //GUEST Functions
        server.addEventListener(
                "register", User.class,
                new RegisterListener(command)
        );
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

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

}
