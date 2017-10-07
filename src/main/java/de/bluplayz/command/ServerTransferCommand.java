package de.bluplayz.command;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import de.bluplayz.BungeeAPI;
import de.bluplayz.Callback;
import de.bluplayz.api.LocaleAPI;
import de.bluplayz.data.PEServer;
import de.bluplayz.network.packet.ServerDataRequestPacket;
import lombok.Getter;

import java.net.InetSocketAddress;

public class ServerTransferCommand extends Command {
    @Getter
    private BungeeAPI plugin;

    public ServerTransferCommand( BungeeAPI plugin ) {
        super( "Server", "", "/server", new String[]{ "transfer" } );

        this.plugin = plugin;
    }

    @Override
    public boolean execute( CommandSender sender, String label, String[] args ) {
        if ( !sender.hasPermission( "network.command.server" ) ) {
            LocaleAPI.sendTranslatedMessage( sender, "command_no_permissions" );
            return false;
        }

        if ( !( sender instanceof Player ) ) {
            if ( args.length <= 1 ) {
                LocaleAPI.sendTranslatedMessage( sender, "command_server_usage_console" );
                return false;
            }

            String targetname = args[0];
            String servername = args[1];

            Player target = getPlugin().getServer().getPlayer( targetname );
            if ( target == null ) {
                LocaleAPI.sendTranslatedMessage( sender, "network_player_not_found" );
                return false;
            }

            ServerDataRequestPacket serverDataRequestPacket = new ServerDataRequestPacket();
            getPlugin().getNettyHandler().addPacketCallback( serverDataRequestPacket, new Callback() {
                @Override
                public void accept( Object... args ) {
                    ServerDataRequestPacket packet = (ServerDataRequestPacket) args[0];

                    PEServer targetServer = null;
                    for ( PEServer server : packet.getServers() ) {
                        if ( !server.isOnline() ) {
                            continue;
                        }

                        if ( server.getName().equalsIgnoreCase( servername ) ) {
                            targetServer = server;
                            break;
                        }
                    }

                    if ( targetServer == null ) {
                        LocaleAPI.sendTranslatedMessage( sender, "network_server_not_found", servername );
                        return;
                    }

                    LocaleAPI.sendTranslatedMessage( sender, "command_server_transfering", targetServer.getName() );
                    target.transfer( new InetSocketAddress( targetServer.getHost(), targetServer.getPort() ) );
                }
            } );

            getPlugin().getPacketHandler().sendPacket( serverDataRequestPacket );
            return true;
        }

        Player player = (Player) sender;

        if ( args.length == 0 ) {
            LocaleAPI.sendTranslatedMessage( sender, "command_server_usage" );
            return false;
        }

        String target = args[0];

        ServerDataRequestPacket serverDataRequestPacket = new ServerDataRequestPacket();
        getPlugin().getNettyHandler().addPacketCallback( serverDataRequestPacket, new Callback() {
            @Override
            public void accept( Object... args ) {
                ServerDataRequestPacket packet = (ServerDataRequestPacket) args[0];

                PEServer targetServer = null;
                for ( PEServer server : packet.getServers() ) {
                    if ( !server.isOnline() ) {
                        continue;
                    }

                    if ( server.getName().equalsIgnoreCase( target ) ) {
                        targetServer = server;
                        break;
                    }
                }

                if ( targetServer == null ) {
                    LocaleAPI.sendTranslatedMessage( sender, "network_server_not_found", target );
                    return;
                }

                LocaleAPI.sendTranslatedMessage( sender, "command_server_transfering", targetServer.getName() );
                player.transfer( new InetSocketAddress( targetServer.getHost(), targetServer.getPort() ) );
            }
        } );

        getPlugin().getPacketHandler().sendPacket( serverDataRequestPacket );
        return false;
    }
}
