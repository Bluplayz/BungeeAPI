package de.bluplayz.util;

import de.bluplayz.networkhandler.netty.packet.Packet;

public interface Callback {
    void accept( Packet packet );
}
