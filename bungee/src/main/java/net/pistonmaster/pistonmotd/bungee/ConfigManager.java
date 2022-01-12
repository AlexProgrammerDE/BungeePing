package net.pistonmaster.pistonmotd.bungee;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.apiguardian.api.API;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ConfigManager {
    private final Plugin plugin;
    private final List<String> headList;

    protected ConfigManager(Plugin plugin, List<String> headList) {
        this.plugin = plugin;
        this.headList = headList;
    }

    @API(status = API.Status.INTERNAL)
    protected Configuration getConfig() {
        Configuration config = null;
        Configuration templateConfig;
        final List<String> configKeys = new ArrayList<>();
        final List<String> templateKeys = new ArrayList<>();

        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdir()) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't create data folder!");
        }

        File file = new File(plugin.getDataFolder(), "config.yml");

        if (!file.exists()) {
            try (InputStream in = plugin.getResourceAsStream("bungeeconfig.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            StringBuilder header;
            StringBuilder content;
            try (BufferedReader in = new BufferedReader(new FileReader(file))) {
                header = new StringBuilder();

                for (String head : headList) {
                    header.append(head).append('\n');
                }

                content = new StringBuilder();

                while (in.ready()) {
                    content.append(in.readLine()).append('\n');
                }
            }

            String output = header + "\n" + content;

            output = output.replace("§", "&");

            FileWriter fStream = new FileWriter(file);
            BufferedWriter out = new BufferedWriter(fStream);

            out.write(output);
            out.close();
        } catch (IOException e) {
            plugin.getLogger().warning("Error: " + e.getMessage());
        }

        // Load config
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert config != null;
        for (String key : config.getKeys()) {
            configKeys.add(key);

            if (config.get(key) instanceof Configuration) {
                iterateKey(key, configKeys, config);
            }
        }

        // Load template
        templateConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(plugin.getResourceAsStream("bungeeconfig.yml"));

        for (String key : templateConfig.getKeys()) {
            templateKeys.add(key);

            if (templateConfig.get(key) instanceof Configuration) {
                iterateKey(key, templateKeys, templateConfig);
            }
        }

        // Check if keys from template are in the config
        for (String key : templateKeys) {
            if (!configKeys.contains(key) || !config.get(key).getClass().equals(templateConfig.get(key).getClass())) {
                config.set(key, templateConfig.get(key));
            }
        }

        // Reload configkeys
        configKeys.clear();

        for (String key : config.getKeys()) {
            configKeys.add(key);

            if (config.get(key) instanceof Configuration) {
                iterateKey(key, configKeys, config);
            }
        }

        // Check if keys from config are in the template
        for (String key : configKeys) {
            if (!templateKeys.contains(key)) {
                config.set(key, null);
            } else if (!templateConfig.get(key).getClass().equals(config.get(key).getClass())) {
                config.set(key, templateConfig.get(key));
            }

            templateKeys.clear();

            // Get template config file
            templateConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(plugin.getResourceAsStream("bungeeconfig.yml"));

            for (String tKey : templateConfig.getKeys()) {
                templateKeys.add(tKey);

                if (templateConfig.get(tKey) instanceof Configuration) {
                    iterateKey(tKey, templateKeys, templateConfig);
                }
            }
        }

        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            StringBuilder header;
            StringBuilder content;
            try (BufferedReader in = new BufferedReader(new FileReader(file))) {
                header = new StringBuilder();

                for (String head : headList) {
                    header.append(head).append('\n');
                }

                content = new StringBuilder();

                while (in.ready()) {
                    content.append(in.readLine()).append('\n');
                }

            }

            String output = header + "\n" + content;

            output = output.replace("§", "&");

            FileWriter fStream = new FileWriter(file);
            try (BufferedWriter out = new BufferedWriter(fStream)) {

                out.write(output);
            }

        } catch (IOException e) {
            plugin.getLogger().warning("Error: " + e.getMessage());
        }

        return config;
    }

    @API(status = API.Status.INTERNAL)
    protected File getIcons() {
        File iconFolder = new File(plugin.getDataFolder(), "icons");

        if (!iconFolder.exists() && !iconFolder.mkdir()) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't create icons folder!");
        }

        return iconFolder;
    }

    private void iterateKey(String gKey, List<String> keyList, Configuration config) {
        for (String key : config.getSection(gKey).getKeys()) {
            keyList.add(gKey + "." + key);

            if (config.get(gKey + "." + key) instanceof Configuration) {
                iterateKey(gKey + "." + key, keyList, config);
            }
        }
    }
}
