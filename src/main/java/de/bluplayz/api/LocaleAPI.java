package de.bluplayz.api;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import de.bluplayz.BungeeAPI;
import de.bluplayz.locale.Locale;

public class LocaleAPI {

    public static void log( String key, Object... args ) {
        String translatedMessage = translate( BungeeAPI.getInstance().getConsoleLocale(), key, args );
        for ( String line : translatedMessage.split( "\\{NEXT_LINE}" ) ) {
            BungeeAPI.getInstance().getServer().getLogger().info( line );
        }
    }

    public static String translate( String languageCode, String key, Object... args ) {
        return translate( BungeeAPI.getInstance().getLocaleManager().getLocale( languageCode ), key, args );
    }

    public static String translate( CommandSender target, String key, Object... args ) {
        if ( target instanceof Player ) {
            String languageCode = ( (Player) target ).getLoginChainData().getLanguageCode();
            return translate( BungeeAPI.getInstance().getLocaleManager().getLocale( languageCode ), key, args );
        }

        return translate( BungeeAPI.getInstance().getConsoleLocale(), key, args );
    }

    public static void sendTranslatedMessage( CommandSender target, String key, Object... args ) {
        target.sendMessage( translate( target, key, args ) );
    }

    public static String translate( Locale locale, String key, Object... args ) {
        String message = BungeeAPI.getInstance().getLocaleManager().getTranslatedMessage( locale, key, args );
        message = message.replaceAll( "\\{PREFIX}", BungeeAPI.getInstance().getLocaleManager().getTranslatedMessage( locale, "prefix" ) );

        return message;
    }
}
