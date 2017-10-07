package de.bluplayz.network.packet;

import com.google.common.base.Charsets;
import de.bluplayz.data.PEServer;
import de.bluplayz.networkhandler.netty.packet.Packet;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@NoArgsConstructor
public class ServerDataRequestPacket extends Packet {

    @Getter
    private ArrayList<PEServer> servers = new ArrayList<>();

    @Override
    public void read( ByteBuf byteBuf ) throws Exception {
        int length;

        // Servers
        int arraySize = byteBuf.readInt();
        for ( int i = 0; i < arraySize; i++ ) {
            PEServer server = new PEServer();

            // Servername
            length = byteBuf.readInt();
            server.setName( (String) byteBuf.readCharSequence( length, Charsets.UTF_8 ) );

            // Host
            length = byteBuf.readInt();
            server.setHost( (String) byteBuf.readCharSequence( length, Charsets.UTF_8 ) );

            // Host
            server.setPort( byteBuf.readInt() );

            // Permission
            length = byteBuf.readInt();
            server.setPermission( (String) byteBuf.readCharSequence( length, Charsets.UTF_8 ) );

            // Online
            server.setOnline( byteBuf.readBoolean() );

            // Players
            int arraySize2 = byteBuf.readInt();
            for ( int i2 = 0; i2 < arraySize2; i2++ ) {
                length = byteBuf.readInt();
                server.getPlayers().add( (String) byteBuf.readCharSequence( length, Charsets.UTF_8 ) );
            }

            getServers().add( server );
        }
    }

    @Override
    public void write( ByteBuf byteBuf ) throws Exception {
        // Servers
        byteBuf.writeInt( getServers().size() );
        for ( PEServer server : getServers() ) {
            // Servername
            byteBuf.writeInt( server.getName().length() );
            byteBuf.writeCharSequence( server.getName(), Charsets.UTF_8 );

            // Host
            byteBuf.writeInt( server.getHost().length() );
            byteBuf.writeCharSequence( server.getHost(), Charsets.UTF_8 );

            // Port
            byteBuf.writeInt( server.getPort() );

            // Permission
            byteBuf.writeInt( server.getPermission().length() );
            byteBuf.writeCharSequence( server.getPermission(), Charsets.UTF_8 );

            // Online
            byteBuf.writeBoolean( server.isOnline() );

            // Players
            byteBuf.writeInt( server.getPlayers().size() );
            for ( String playername : server.getPlayers() ) {
                byteBuf.writeInt( playername.length() );
                byteBuf.writeCharSequence( playername, Charsets.UTF_8 );
            }
        }
    }
}
