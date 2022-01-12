package net.pistonmaster.pistonmotd.sponge;

import com.google.inject.Inject;
import net.kyori.adventure.platform.spongeapi.SpongeAudiences;
import net.pistonmaster.pistonmotd.api.PlaceholderUtil;
import net.pistonmaster.pistonmotd.data.PluginData;
import net.pistonmaster.pistonmotd.utils.ConsoleColor;
import net.pistonmaster.pistonmotd.utils.LuckPermsWrapper;
import net.pistonmaster.pistonutils.logging.PistonLogger;
import net.pistonmaster.pistonutils.update.UpdateChecker;
import net.pistonmaster.pistonutils.update.UpdateParser;
import net.pistonmaster.pistonutils.update.UpdateType;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.bstats.sponge.Metrics;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.metric.MetricsConfigManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Plugin(id = "pistonmotd", name = PluginData.NAME, version = PluginData.VERSION, description = PluginData.DESCRIPTION, url = PluginData.URL, authors = {"AlexProgrammerDE"})
public class PistonMOTDSponge {
    private final Metrics.Factory metricsFactory;
    protected ConfigurationNode rootNode;
    protected File icons;
    protected LuckPermsWrapper luckpermsWrapper = null;
    @Inject
    protected Game game;
    @Inject
    private Logger log;
    @Inject
    @DefaultConfig(sharedRoot = true)
    private Path defaultConfig;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    @Inject
    @ConfigDir(sharedRoot = true)
    private Path publicConfigDir;

    @Inject
    private PluginContainer container;

    @Inject
    private MetricsConfigManager metricsConfigManager;

    @Inject
    public PistonMOTDSponge(Metrics.Factory metricsFactory) {
        this.metricsFactory = metricsFactory;
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        SpongeAudiences.create(container, game);

        log.info("  _____  _       _                 __  __   ____  _______  _____  ");
        log.info(" |  __ \\(_)     | |               |  \\/  | / __ \\|__   __||  __ \\ ");
        log.info(" | |__) |_  ___ | |_  ___   _ __  | \\  / || |  | |  | |   | |  | |");
        log.info(" |  ___/| |/ __|| __|/ _ \\ | '_ \\ | |\\/| || |  | |  | |   | |  | |");
        log.info(" | |    | |\\__ \\| |_| (_) || | | || |  | || |__| |  | |   | |__| |");
        log.info(" |_|    |_||___/ \\__|\\___/ |_| |_||_|  |_| \\____/   |_|   |_____/ ");
        log.info("                                                                  ");

        log.info(ConsoleColor.CYAN + "Loading config" + ConsoleColor.RESET);
        loadConfig();

        log.info(ConsoleColor.CYAN + "Registering command" + ConsoleColor.RESET);
        CommandSpec help = CommandSpec.builder()
                .description(Text.of("Get help about PistonMOTD!"))
                .permission("pistonmotd.help")
                .executor(new SpongeHelpCommand())
                .build();

        CommandSpec reload = CommandSpec.builder()
                .description(Text.of("Reload the configuration of PistonMOTD!"))
                .permission("pistonmotd.reload")
                .executor(new SpongeReloadCommand(this))
                .build();

        CommandSpec command = CommandSpec.builder()
                .description(Text.of("Main command of PistonMOTD!"))
                .child(help, "help")
                .child(reload, "reload")
                .build();

        game.getCommandManager().register(this, command, "pistonmotd", "pistonmotdsponge");

        log.info(ConsoleColor.CYAN + "Registering placeholders" + ConsoleColor.RESET);
        PlaceholderUtil.registerParser(new CommonPlaceholder(game));

        log.info(ConsoleColor.CYAN + "Looking for hooks" + ConsoleColor.RESET);
        if (game.getPluginManager().getPlugin("luckperms").isPresent()) {
            try {
                log.info(ConsoleColor.CYAN + "Hooking into LuckPerms" + ConsoleColor.RESET);
                luckpermsWrapper = new LuckPermsWrapper();
            } catch (Exception ignored) {
            }
        }

        log.info(ConsoleColor.CYAN + "Registering listeners" + ConsoleColor.RESET);
        game.getEventManager().registerListeners(this, new PingEvent(this));
        game.getEventManager().registerListeners(this, new JoinEvent(this));

        if (container.getVersion().isPresent() && rootNode.getNode("updatechecking").getBoolean()) {
            log.info(ConsoleColor.CYAN + "Checking for a newer version" + ConsoleColor.RESET);
            new UpdateChecker(new PistonLogger(log)).getVersion("https://www.pistonmaster.net/PistonMOTD/VERSION.txt", version -> new UpdateParser(container.getVersion().get(), version).parseUpdate(updateType -> {
                if (updateType == UpdateType.NONE || updateType == UpdateType.AHEAD) {
                    log.info(ConsoleColor.CYAN + "You're up to date!" + ConsoleColor.RESET);
                } else {
                    if (updateType == UpdateType.MAJOR) {
                        log.info(ConsoleColor.RED + "There is a MAJOR update available!" + ConsoleColor.RESET);
                    } else if (updateType == UpdateType.MINOR) {
                        log.info(ConsoleColor.RED + "There is a MINOR update available!" + ConsoleColor.RESET);
                    } else if (updateType == UpdateType.PATCH) {
                        log.info(ConsoleColor.RED + "There is a PATCH update available!" + ConsoleColor.RESET);
                    }

                    log.info(ConsoleColor.RED + "Current version: " + container.getVersion().get() + " New version: " + version + ConsoleColor.RESET);
                    log.info(ConsoleColor.RED + "Download it at: https://ore.spongepowered.org/AlexProgrammerDE/PistonMOTD/versions" + ConsoleColor.RESET);
                }
            }));
        }

        final Tristate collectionState = this.getEffectiveCollectionState();
        if (collectionState == Tristate.TRUE) {
            log.info(ConsoleColor.CYAN + "Loading metrics" + ConsoleColor.RESET);
            metricsFactory.make(9204);
        } else if (collectionState == Tristate.UNDEFINED) {
            log.info(ConsoleColor.CYAN + "Hey there! It seems like data collection is disabled. :( " + ConsoleColor.RESET);
            log.info(ConsoleColor.CYAN + "But don't worry... You can fix this! " + ConsoleColor.RESET);
            log.info(ConsoleColor.CYAN + "Just execute: \"/sponge metrics pistonmotd enable\"." + ConsoleColor.RESET);
            log.info(ConsoleColor.CYAN + "This info is just to give me small info about the server," + ConsoleColor.RESET);
            log.info(ConsoleColor.CYAN + "like its version and the plugin version." + ConsoleColor.RESET);
        }

        log.info(ConsoleColor.CYAN + "Done! :D" + ConsoleColor.RESET);
    }

    protected void loadConfig() {
        final File oldConfigFile = new File(publicConfigDir.toFile(), "pistonmotd.yml");

        try {

            Optional<Asset> optionalAsset = container.getAsset("sponge.conf");
            if (optionalAsset.isPresent()) {
                ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setPath(defaultConfig).build();

                if (oldConfigFile.exists()) {
                    loader.save(YAMLConfigurationLoader.builder().setFile(oldConfigFile).build().load());

                    Files.delete(oldConfigFile.toPath());
                }

                Asset asset = optionalAsset.get();

                asset.copyToFile(defaultConfig, false, true);

                rootNode = loader.load();

                rootNode.mergeValuesFrom(HoconConfigurationLoader.builder().setURL(asset.getUrl()).build().load());

                loader.save(rootNode);

                File iconFolder = new File(publicConfigDir.toFile(), "icons");

                if (!iconFolder.exists() && !iconFolder.mkdir()) {
                    throw new IOException("Couldn't create folder!");
                }

                icons = iconFolder;
            } else {
                throw new IOException("Default configuration file missing in jar!!!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the effective collection state for the plugin.
     * <p>
     * This will return the global collection state fallback, if the server administrator doesn't
     * have a specific collection state for the plugin.
     *
     * @return The collection state
     */
    protected Tristate getEffectiveCollectionState() {
        final Tristate pluginState = this.metricsConfigManager.getCollectionState(this.container);
        if (pluginState == Tristate.UNDEFINED) {
            return this.metricsConfigManager.getGlobalCollectionState();
        }
        return pluginState;
    }
}
