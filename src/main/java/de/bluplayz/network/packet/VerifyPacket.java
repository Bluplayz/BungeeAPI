package de.bluplayz.network.packet;

import com.google.common.base.Charsets;
import de.bluplayz.networkhandler.netty.packet.Packet;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class VerifyPacket extends Packet {
    @Setter
    @Getter
    private String servername = "";

    @Setter
    @Getter
    private String host = "";

    @Setter
    @Getter
    private int port = 19133;

    @Setter
    @Getter
    private boolean success = false;

    @Override
    public void read( ByteBuf byteBuf ) throws Exception {
        int length;

        // Servername
        length = byteBuf.readInt();
        setServername( (String) byteBuf.readCharSequence( length, Charsets.UTF_8 ) );

        // Host
        length = byteBuf.readInt();
        setHost( (String) byteBuf.readCharSequence( length, Charsets.UTF_8 ) );

        // Port
        setPort( byteBuf.readInt() );

        // Success
        setSuccess( byteBuf.readBoolean() );
    }

    @Override
    public void write( ByteBuf byteBuf ) throws Exception {
        // Servername
        byteBuf.writeInt( getServername().length() );
        byteBuf.writeCharSequence( getServername(), Charsets.UTF_8 );

        // Host
        byteBuf.writeInt( getHost().length() );
        byteBuf.writeCharSequence( getHost(), Charsets.UTF_8 );

        // Port
        byteBuf.writeInt( getPort() );

        // Success
        byteBuf.writeBoolean( isSuccess() );
    }
}
