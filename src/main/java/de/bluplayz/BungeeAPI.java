package de.bluplayz;

import cn.nukkit.Player;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.NukkitRunnable;
import de.bluplayz.api.LocaleAPI;
import de.bluplayz.command.ServerTransferCommand;
import de.bluplayz.command.ServerlistCommand;
import de.bluplayz.data.MySQL;
import de.bluplayz.listener.PlayerCommandPreprocessListener;
import de.bluplayz.listener.PlayerLoginListener;
import de.bluplayz.listener.PlayerQuitListener;
import de.bluplayz.locale.Locale;
import de.bluplayz.network.packet.*;
import de.bluplayz.networkhandler.netty.ConnectionListener;
import de.bluplayz.networkhandler.netty.NettyHandler;
import de.bluplayz.networkhandler.netty.PacketHandler;
import de.bluplayz.networkhandler.netty.packet.Packet;
import de.bluplayz.networkhandler.netty.packet.defaultpackets.SetNamePacket;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;

import java.net.InetSocketAddress;
import java.util.HashMap;

public class BungeeAPI extends PluginBase {
    public static int PORT = 19080;
    public static String HOST = "localhost";

    @Getter
    private static BungeeAPI instance;

    @Getter
    private LocaleManager localeManager;

    @Getter
    private Locale consoleLocale;

    @Getter
    private MySQL mySQL;

    @Getter
    private NettyHandler nettyHandler;

    @Getter
    private PacketHandler packetHandler;

    @Getter
    private ConnectionListener connectionListener;

    @Getter
    private boolean verified = false;

    public BungeeAPI() {
        // Save instance for further use
        BungeeAPI.instance = this;
    }

    @Override
    public void onEnable() {
        // Check configdata
        this.initConfig();

        // Initialize locale system
        this.initLocales();

        // Loading start
        LocaleAPI.log( "console_loading_message_start", this.getName(), getDescription().getVersion() );

        // Initialize DataHandler
        this.initData();

        // Register Commands
        this.registerCommands();

        // Register Events
        registerEvents();

        // Initialize netty connection
        initNetwork();

        // Loading finished
        LocaleAPI.log( "console_loading_message_finish", this.getName(), getDescription().getVersion() );

        // Send current Locale message
        LocaleAPI.log( "console_language_set_success" );
    }

    @Override
    public void onDisable() {
        if ( this.getMySQL() != null ) {
            //getMySQL().disconnect();
        }
    }

    /**
     * init config files
     */
    private void initConfig() {
        // Save default config
        this.saveDefaultConfig();

        // Init some data from config
        BungeeAPI.HOST = this.getConfig().getString( "data.netty.host" );
        BungeeAPI.PORT = this.getConfig().getInt( "data.netty.port" );
    }

    /**
     * register commands
     */
    private void registerCommands() {
        getServer().getCommandMap().register( "blu", new ServerlistCommand( this ) );
        getServer().getCommandMap().register( "blu", new ServerTransferCommand( this ) );
    }

    /**
     * register events
     */
    private void registerEvents() {
        getServer().getPluginManager().registerEvents( new PlayerCommandPreprocessListener( this ), this );
        getServer().getPluginManager().registerEvents( new PlayerLoginListener( this ), this );
        getServer().getPluginManager().registerEvents( new PlayerQuitListener( this ), this );
    }

    /**
     * init datahandler and default data
     */
    private void initData() {
        this.mySQL = new MySQL();
    }

    /**
     * init locales with default translations
     */
    private void initLocales() {
        HashMap<String, String> translations = new HashMap<>();

        // Initialize LocaleManager
        this.localeManager = new LocaleManager( getDataFolder() + "/locales" );

        /** GERMAN */
        Locale germanLocale = getLocaleManager().createLocale( "de_DE" );

        translations.clear();
        translations.put( "prefix", "§7[§3BungeePE§7]§r" );
        translations.put( "console_loading_message_start", "{PREFIX} §a{0} v{1} wird geladen..." );
        translations.put( "console_loading_message_finish", "{PREFIX} §a{0} v{1} wurde erfolgreich geladen!" );
        translations.put( "console_language_set_success", "{PREFIX} §7Die Sprache der Konsole ist §bDeutsch§7." );
        translations.put( "data_mysql_driver_not_found", "{PREFIX} §cDie MySQL Treiber wurden nicht gefunden!" );
        translations.put( "data_mysql_connected_successfully", "{PREFIX} §aDie Verbindung zu der MySQL Datenbank wurde hergestellt." );
        translations.put( "data_netty_start_connecting", "{PREFIX} §aVerbinde zum Proxy Server..." );
        translations.put( "data_netty_connected_successfully", "{PREFIX} §aEs wurde erfolgreich ein Verbindung zum Proxy auf Port {0} erstellt." );
        translations.put( "data_netty_connection_lost", "{PREFIX} §cVerbindung zum Proxy wurde unterbrochen!" );
        translations.put( "network_server_not_found", "{PREFIX} §cDer Server {0} wurde nicht gefunden!" );
        translations.put( "network_player_not_found", "{PREFIX} §cDer Spieler {0} wurde nicht gefunden!" );
        translations.put( "command_no_permissions", "{PREFIX} §cDu hast keine Berechtigung diesen Command auszuführen!" );
        translations.put( "command_servers_online_servers", "{PREFIX} §3Online Server:" );
        translations.put( "command_server_usage", "{PREFIX} §eBenutzung: /server <servername>" );
        translations.put( "command_server_usage_console", "{PREFIX} §eBenutzung: /server <spieler> <servername>" );
        translations.put( "command_server_transfering", "{PREFIX} §7Du wirst nun zum Server §b{0} §7geportet." );

        germanLocale.addTranslations( translations, false );
        /** GERMAN */

        /** ENGLISH */
        Locale englishLocale = getLocaleManager().createLocale( "en_EN" );

        translations.clear();
        translations.put( "prefix", "§7[§3BungeePE§7]§r" );
        translations.put( "console_loading_message_start", "{PREFIX} §aLoading {0} v{1}..." );
        translations.put( "console_loading_message_finish", "{PREFIX} §aSuccessfully loaded {0} v{1}!" );
        translations.put( "console_language_set_success", "{PREFIX} §7The Language of the Console is §bEnglish§7." );
        translations.put( "data_mysql_driver_not_found", "{PREFIX} §cDriver for MySQL was not found!" );
        translations.put( "data_mysql_connected_successfully", "{PREFIX} §aSuccessfully connected to the MySQL Database." );
        translations.put( "data_netty_start_connecting", "{PREFIX} §aConnecting to Proxy..." );
        translations.put( "data_netty_connected_successfully", "{PREFIX} §aSuccessfully connected to Proxy on Port {0}" );
        translations.put( "data_netty_connection_lost", "{PREFIX} §cConnection to Proxy lost!" );
        translations.put( "network_server_not_found", "{PREFIX} §cThe Server {0} was not found!" );
        translations.put( "network_player_not_found", "{PREFIX} §cThe Player {0} was not found!" );
        translations.put( "command_no_permissions", "{PREFIX} §cYou don't have the permission to perform this command!" );
        translations.put( "command_servers_online_servers", "{PREFIX} §3Online Servers:" );
        translations.put( "command_server_usage", "{PREFIX} §eUsage: /server <servername>" );
        translations.put( "command_server_usage_console", "{PREFIX} §eUsage: /server <player> <servername>" );
        translations.put( "command_server_transfering", "{PREFIX} §7You will be transfered to the Server §b{0}§7." );

        englishLocale.addTranslations( translations, false );
        /** ENGLISH */

        // Set Console locale
        this.consoleLocale = getLocaleManager().getLocale( getConfig().getString( "language.console" ) );

        // Set default locale
        this.getLocaleManager().setDefaultLocale( getLocaleManager().getLocale( getConfig().getString( "language.fallback" ) ) );
    }

    private void initNetwork() {
        this.nettyHandler = new NettyHandler();
        this.nettyHandler.connectToServer( HOST, PORT, new Callback() {
            @Override
            public void accept( Object... args ) {
            }
        } );

        // Loading message start
        LocaleAPI.log( "data_netty_start_connecting" );

        // Init packet handler
        this.getNettyHandler().registerPacketHandler( this.packetHandler = new PacketHandler() {
            @Override
            public void incomingPacket( Packet packet, Channel channel ) {
                if ( packet instanceof VerifyPacket ) {
                    VerifyPacket verifyPacket = (VerifyPacket) packet;
                    if ( verifyPacket.isSuccess() ) {
                        verified = true;
                    }
                    return;
                }

                if ( packet instanceof PlayerTransferPacket ) {
                    PlayerTransferPacket playerTransferPacket = (PlayerTransferPacket) packet;
                    Player player = getServer().getPlayer( playerTransferPacket.getPlayername() );
                    if ( player == null ) {
                        return;
                    }

                    player.transfer( new InetSocketAddress( playerTransferPacket.getHost(), playerTransferPacket.getPort() ) );
                    playerTransferPacket.setSuccess( true );
                    sendPacket( playerTransferPacket, channel );
                    return;
                }
            }

            @Override
            public void registerPackets() {
                registerPacket( VerifyPacket.class );
                registerPacket( PEServerDataPacket.class );
                registerPacket( PlayerCommandEnteredPacket.class );
                registerPacket( ServerDataRequestPacket.class );
                registerPacket( PlayerTransferPacket.class );
                registerPacket( PlayerConnectPacket.class );
                registerPacket( PlayerDisconnectPacket.class );
            }
        } );

        // Initialize connection listener
        this.getNettyHandler().registerConnectionListener( this.connectionListener = new ConnectionListener() {
            @Override
            public void channelConnected( ChannelHandlerContext ctx ) {
                // Loading message finish
                LocaleAPI.log( "data_netty_connected_successfully", PORT );

                // Set name for this netty connection to the servername
                SetNamePacket setNamePacket = new SetNamePacket( getServername() );
                BungeeAPI.this.getPacketHandler().sendPacket( setNamePacket );
            }

            @Override
            public void channelDisconnected( ChannelHandlerContext ctx ) {
                LocaleAPI.log( "data_netty_connection_lost" );

                verified = false;
                BungeeAPI.this.getNettyHandler().getNettyClient().scheduleConnect( 1500 );
            }
        } );

        // Refresh data
        new NukkitRunnable() {
            @Override
            public void run() {
                //check if verified
                if ( !isVerified() ) {
                    VerifyPacket verifyPacket = new VerifyPacket();
                    verifyPacket.setServername( getServername() );
                    verifyPacket.setHost( getServer().getIp() );
                    verifyPacket.setPort( getServer().getPort() );

                    BungeeAPI.this.getPacketHandler().sendPacket( verifyPacket );
                }

                /*
                PEServerDataPacket peServerDataPacket = new PEServerDataPacket();
                peServerDataPacket.setServername( getServername() );

                for ( Player player : BungeeAPI.this.getServer().getOnlinePlayers().values() ) {
                    //peServerDataPacket.getPlayers().add( player.getName() );
                }

                BungeeAPI.this.getPacketHandler().sendPacket( peServerDataPacket );
                */
            }
        }.runTaskTimer( this, 0, 20 );
    }

    public String getServername() {
        return getServer().getMotd();
    }
}
