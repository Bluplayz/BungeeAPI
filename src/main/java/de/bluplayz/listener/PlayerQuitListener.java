package de.bluplayz.listener;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.player.PlayerQuitEvent;
import de.bluplayz.BungeeAPI;
import de.bluplayz.network.packet.PlayerDisconnectPacket;

public class PlayerQuitListener extends SimpleListener {
    public PlayerQuitListener( BungeeAPI plugin ) {
        super( plugin );
    }

    @EventHandler( priority = EventPriority.LOW )
    public void onQuit( PlayerQuitEvent e ) {
        Player player = e.getPlayer();

        if ( !e.getReason().contains( "Transfer" ) ) {
            PlayerDisconnectPacket playerDisconnectPacket = new PlayerDisconnectPacket();
            playerDisconnectPacket.setServername( getPlugin().getServername() );
            playerDisconnectPacket.setPlayername( player.getName() );

            getPlugin().getPacketHandler().sendPacket( playerDisconnectPacket );
        }
    }
}
