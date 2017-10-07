package de.bluplayz.network.packet;

import com.google.common.base.Charsets;
import de.bluplayz.networkhandler.netty.packet.Packet;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class PlayerDisconnectPacket extends Packet {
    @Setter
    @Getter
    private String playername = "";

    @Setter
    @Getter
    private String servername = "";

    @Override
    public void read( ByteBuf byteBuf ) throws Exception {
        int length;

        // Playername
        length = byteBuf.readInt();
        setPlayername( (String) byteBuf.readCharSequence( length, Charsets.UTF_8 ) );

        // Servername
        length = byteBuf.readInt();
        setServername( (String) byteBuf.readCharSequence( length, Charsets.UTF_8 ) );
    }

    @Override
    public void write( ByteBuf byteBuf ) throws Exception {
        // Playername
        byteBuf.writeInt( getPlayername().length() );
        byteBuf.writeCharSequence( getPlayername(), Charsets.UTF_8 );

        // Servername
        byteBuf.writeInt( getServername().length() );
        byteBuf.writeCharSequence( getServername(), Charsets.UTF_8 );
    }
}
