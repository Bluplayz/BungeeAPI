package de.bluplayz.listener;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.player.PlayerLoginEvent;
import de.bluplayz.BungeeAPI;
import de.bluplayz.network.packet.PlayerConnectPacket;

public class PlayerLoginListener extends SimpleListener {
    public PlayerLoginListener( BungeeAPI plugin ) {
        super( plugin );
    }

    @EventHandler( priority = EventPriority.LOW )
    public void onLogin( PlayerLoginEvent e ) {
        Player player = e.getPlayer();

        PlayerConnectPacket playerConnectPacket = new PlayerConnectPacket();
        playerConnectPacket.setServername( getPlugin().getServername() );
        playerConnectPacket.setPlayername( player.getName() );

        getPlugin().getPacketHandler().sendPacket( playerConnectPacket );
    }
}
