package de.bluplayz.listener;

import cn.nukkit.event.Listener;
import de.bluplayz.BungeeAPI;
import lombok.Getter;

public class SimpleListener implements Listener {
    @Getter
    private BungeeAPI plugin;

    public SimpleListener( BungeeAPI plugin ) {
        this.plugin = plugin;
    }
}
