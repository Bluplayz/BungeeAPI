package de.bluplayz.listener;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.player.PlayerCommandPreprocessEvent;
import de.bluplayz.BungeeAPI;
import de.bluplayz.network.packet.PlayerCommandEnteredPacket;

public class PlayerCommandPreprocessListener extends SimpleListener {
    public PlayerCommandPreprocessListener( BungeeAPI plugin ) {
        super( plugin );
    }

    @EventHandler( priority = EventPriority.LOW )
    public void onCmd( PlayerCommandPreprocessEvent e ) {
        Player player = e.getPlayer();
        String message = e.getMessage();

        PlayerCommandEnteredPacket playerCommandEnteredPacket = new PlayerCommandEnteredPacket();
        playerCommandEnteredPacket.setServername( getPlugin().getServername() );
        playerCommandEnteredPacket.setPlayername( player.getName() );
        playerCommandEnteredPacket.setCommandMessage( message );

        getPlugin().getPacketHandler().sendPacket( playerCommandEnteredPacket );
    }
}
