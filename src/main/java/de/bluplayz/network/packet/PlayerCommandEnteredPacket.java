package de.bluplayz.network.packet;

import com.google.common.base.Charsets;
import de.bluplayz.networkhandler.netty.packet.Packet;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class PlayerCommandEnteredPacket extends Packet {

    @Setter
    @Getter
    private String servername = "";

    @Setter
    @Getter
    private String playername = "";

    @Setter
    @Getter
    private String commandMessage = "";

    @Override
    public void read( ByteBuf byteBuf ) throws Exception {
        int length;

        // Servername
        length = byteBuf.readInt();
        setServername( (String) byteBuf.readCharSequence( length, Charsets.UTF_8 ) );

        // Playername
        length = byteBuf.readInt();
        setPlayername( (String) byteBuf.readCharSequence( length, Charsets.UTF_8 ) );

        // CommandMessage
        length = byteBuf.readInt();
        setCommandMessage( (String) byteBuf.readCharSequence( length, Charsets.UTF_8 ) );
    }

    @Override
    public void write( ByteBuf byteBuf ) throws Exception {
        // Servername
        byteBuf.writeInt( getServername().length() );
        byteBuf.writeCharSequence( getServername(), Charsets.UTF_8 );

        // Playername
        byteBuf.writeInt( getPlayername().length() );
        byteBuf.writeCharSequence( getPlayername(), Charsets.UTF_8 );

        // CommandMessage
        byteBuf.writeInt( getCommandMessage().length() );
        byteBuf.writeCharSequence( getCommandMessage(), Charsets.UTF_8 );
    }
}
