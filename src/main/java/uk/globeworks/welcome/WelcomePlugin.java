package uk.globeworks.welcome;

import org.bukkit.plugin.java.JavaPlugin;

import uk.globeworks.welcome.command.CommandHandler;
import uk.globeworks.welcome.config.Config;
import uk.globeworks.welcome.listener.PlayerListener;
import uk.globeworks.welcome.util.config.ConfigRegistry;
import uk.globeworks.welcome.util.config.JsonConfigRegistry;

import java.io.File;
import java.util.Objects;
import java.util.logging.Logger;

public class WelcomePlugin extends JavaPlugin {



    @Override
    public void onEnable() {
        getDataFolder().mkdirs();
        final Logger logger = getLogger();
        logger.info(Globeworks.logo("WelcomeBack", getDescription().getVersion()));        
        final ConfigRegistry configRegistry = new JsonConfigRegistry();

        final Config config = configRegistry.register(Config.class, new Config(), new File(getDataFolder(), "config.json"));

        final PlayerListener playerListener = new PlayerListener(this, config);

        Objects.requireNonNull(getCommand("welcome")).setExecutor(new CommandHandler(this, playerListener, config));

    }
}
