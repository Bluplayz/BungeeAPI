package de.bluplayz.network.packet;

import com.google.common.base.Charsets;
import de.bluplayz.networkhandler.netty.packet.Packet;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class PlayerTransferPacket extends Packet {
    @Setter
    @Getter
    private String playername = "";

    @Setter
    @Getter
    private String host = "";

    @Setter
    @Getter
    private int port = 19133;

    @Override
    public void read( ByteBuf byteBuf ) throws Exception {
        int length;

        // Playername
        length = byteBuf.readInt();
        setPlayername( (String) byteBuf.readCharSequence( length, Charsets.UTF_8 ) );

        // Host
        length = byteBuf.readInt();
        setHost( (String) byteBuf.readCharSequence( length, Charsets.UTF_8 ) );

        // Port
        setPort( byteBuf.readInt() );
    }

    @Override
    public void write( ByteBuf byteBuf ) throws Exception {
        // Playername
        byteBuf.writeInt( getPlayername().length() );
        byteBuf.writeCharSequence( getPlayername(), Charsets.UTF_8 );

        // Host
        byteBuf.writeInt( getHost().length() );
        byteBuf.writeCharSequence( getHost(), Charsets.UTF_8 );

        // Port
        byteBuf.writeInt( getPort() );
    }
}
