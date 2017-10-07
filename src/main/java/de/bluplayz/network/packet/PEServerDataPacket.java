package de.bluplayz.network.packet;

import com.google.common.base.Charsets;
import de.bluplayz.networkhandler.netty.packet.Packet;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@NoArgsConstructor
public class PEServerDataPacket extends Packet {

    @Setter
    @Getter
    private String servername = "";

    @Getter
    private ArrayList<String> players = new ArrayList<>();

    @Override
    public void read( ByteBuf byteBuf ) throws Exception {
        int length;

        // Servername
        length = byteBuf.readInt();
        setServername( (String) byteBuf.readCharSequence( length, Charsets.UTF_8 ) );

        // Players
        int arraySize = byteBuf.readInt();
        for ( int i = 0; i < arraySize; i++ ) {
            length = byteBuf.readInt();
            String playername = (String) byteBuf.readCharSequence( length, Charsets.UTF_8 );
            getPlayers().add( playername );
        }
    }

    @Override
    public void write( ByteBuf byteBuf ) throws Exception {
        // Servername
        byteBuf.writeInt( getServername().length() );
        byteBuf.writeCharSequence( getServername(), Charsets.UTF_8 );

        // Players
        byteBuf.writeInt( getPlayers().size() );
        for ( String playername : getPlayers() ) {
            byteBuf.writeInt( playername.length() );
            byteBuf.writeCharSequence( playername, Charsets.UTF_8 );
        }
    }
}
