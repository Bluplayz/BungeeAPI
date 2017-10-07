package de.bluplayz.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;

@NoArgsConstructor
public class PEServer {
    public static ArrayList<PEServer> servers = new ArrayList<>();
    public static HashMap<Integer, PEServer> serverJoinPriority = new HashMap<>();

    @Setter
    @Getter
    private String name = "";

    @Setter
    @Getter
    private String host = "";

    @Setter
    @Getter
    private int port = 19133;

    @Setter
    @Getter
    private boolean online = false;

    @Setter
    @Getter
    private String permission = "";

    @Setter
    @Getter
    private ArrayList<String> players = new ArrayList<>();

    public static PEServer getServerByName( String servername ) {
        for ( PEServer server : servers ) {
            if ( server.getName().equalsIgnoreCase( servername ) ) {
                return server;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return "PEServer{" +
                "name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", online=" + online +
                ", permission='" + permission + '\'' +
                ", players=" + players +
                '}';
    }
}
