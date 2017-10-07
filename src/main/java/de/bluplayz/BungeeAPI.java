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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashMap;

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

        // Check for update
        this.checkForUpdate();

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
     * check for update
     */
    private void checkForUpdate() {
        try {
            LocaleAPI.log( "updater_check_message" );
            String version = "error";
            String updateMessage = "update message was not found";

            URL url = new URL( "https://raw.githubusercontent.com/Bluplayz/BungeeAPI/master/src/main/resources/plugin.yml" );
            URLConnection connection = url.openConnection();

            BufferedReader in = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );
            String line;
            while ( ( line = in.readLine() ) != null ) {
                if ( line.startsWith( "version: " ) ) {
                    version = line.substring( 9 );
                    break;
                }
            }

            in.close();

            url = new URL( "https://raw.githubusercontent.com/Bluplayz/BungeeAPI/master/UpdateNotes.yml" );
            connection = url.openConnection();

            in = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );
            while ( ( line = in.readLine() ) != null ) {
                if ( line.startsWith( version + ": " ) ) {
                    updateMessage = line.substring( version.length() + 2 );
                    break;
                }
            }

            in.close();

            if ( !version.equalsIgnoreCase( getDescription().getVersion() ) ) {
                LocaleAPI.log( "updater_new_version_available", version, updateMessage, "https://github.com/Bluplayz/BungeeAPI" );
            } else {
                LocaleAPI.log( "updater_already_up_to_date" );
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        if ( getConfig().getBoolean( "autoupdater.activated" ) ) {
            new NukkitRunnable() {
                @Override
                public void run() {
                    checkForUpdate();
                }
            }.runTaskLater( this, getConfig().getInt( "autoupdater.checkForUpdate" ) * 20 );
        }
    }

    /**
     * init config files
     */
    private void initConfig() {
        this.getConfig().reload();

        boolean edited = false;

        // LANGUAGE
        if ( !this.getConfig().exists( "language.console" ) ) {
            this.getConfig().set( "language.console", "de_DE" );
            edited = true;
        }
        if ( !this.getConfig().exists( "language.fallback" ) ) {
            this.getConfig().set( "language.fallback", "en_EN" );
            edited = true;
        }

        // UPDATER
        if ( !this.getConfig().exists( "autoupdater" ) ) {
            this.getConfig().set( "autoupdater.activated", true );
            this.getConfig().set( "autoupdater.checkForUpdate", 30 * 60 );
            edited = true;
        }

        // DATA
        if ( !this.getConfig().exists( "data.mysql" ) ) {
            this.getConfig().set( "data.mysql.host", "localhost" );
            this.getConfig().set( "data.mysql.port", 3306 );
            this.getConfig().set( "data.mysql.username", "root" );
            this.getConfig().set( "data.mysql.database", "bungeepe" );
            this.getConfig().set( "data.mysql.password", "" );
            edited = true;
        }
        if ( !this.getConfig().exists( "data.proxy.address" ) ) {
            this.getConfig().set( "data.proxy.address", "localhost:19132" );
            edited = true;
        }

        if ( edited ) {
            this.getConfig().save();
            this.getConfig().reload();
        }

        // Init some data from config
        BungeeAPI.HOST = this.getConfig().getString( "data.proxy.address" ).split( ":" )[0];
        BungeeAPI.PORT = Integer.valueOf( this.getConfig().getString( "data.proxy.address" ).split( ":" )[1] );
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
        LinkedHashMap<String, String> translations = new LinkedHashMap<>();

        // Initialize LocaleManager
        this.localeManager = new LocaleManager( getDataFolder() + "/locales" );

        /** GERMAN */
        Locale germanLocale = getLocaleManager().createLocale( "de_DE" );

        translations.clear();
        translations.put( "prefix", "§7[§3BungeePE§7]§r" );
        translations.put( "updater_check_message", "{PREFIX} §aSuche nach Updates..." );
        translations.put( "updater_already_up_to_date", "{PREFIX} §aDu hast bereits die neuste Version!" );
        translations.put( "updater_new_version_available", "{PREFIX} {NEXT_LINE}" +
                "{PREFIX} §aEine neue Version ist verfuegbar! {NEXT_LINE}" +
                "{PREFIX} §aVersion§7: §b{0} {NEXT_LINE}" +
                "{PREFIX} §aUpdates§7: §b{1} {NEXT_LINE}" +
                "{PREFIX} {NEXT_LINE}" +
                "{PREFIX} §aDen Downloadlink gibt es hier: §b{2}{NEXT_LINE}" +
                "{PREFIX}" );
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
        translations.put( "updater_check_message", "{PREFIX} §aChecking for update..." );
        translations.put( "updater_already_up_to_date", "{PREFIX} §aYou already have the newest Version!" );
        translations.put( "updater_new_version_available", "{PREFIX}{NEXT_LINE}" +
                "{PREFIX} §aA new Version is Available! {NEXT_LINE}" +
                "{PREFIX} §aVersion§7: §b{0} {NEXT_LINE}" +
                "{PREFIX} §aUpdates§7: §b{1} {NEXT_LINE}" +
                "{PREFIX} {NEXT_LINE}" +
                "{PREFIX} §aYou can download it here: §b{2}{NEXT_LINE}" +
                "{PREFIX}" );
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
                    } else {
                        getServer().getLogger().info( "§cInvalid Connection Data... Check your IP, Port and Servername and restarts your server then" );
                        getServer().getLogger().info( "§cPlugin will be disabled" );
                        getNettyHandler().getNettyClient().disconnect();
                        getNettyHandler().unregisterAllPacketHandler();
                        getNettyHandler().unregisterAllConnectionListener();

                        new NukkitRunnable() {
                            @Override
                            public void run() {
                                getServer().getPluginManager().disablePlugin( BungeeAPI.getInstance() );
                            }
                        }.runTaskLater( BungeeAPI.this, 20 );
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
