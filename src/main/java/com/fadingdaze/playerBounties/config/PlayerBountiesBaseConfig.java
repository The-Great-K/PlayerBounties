package com.fadingdaze.playerBounties.config;

import com.fadingdaze.playerBounties.PlayerBounties;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

public class PlayerBountiesBaseConfig {
    private static final PlayerBountiesBaseConfig INSTANCE = new PlayerBountiesBaseConfig();

    private File file;
    private YamlConfiguration config;

    public static PlayerBountiesBaseConfig getInstance() {
        return INSTANCE;
    }

    public void load() {
        file = new File(PlayerBounties.getInstance().getDataFolder(), "config.yml");

        if (!file.exists()) {
            PlayerBounties.getInstance().saveResource("config.yml", false);
        }

        config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        PlayerBounties.defaultBountyDuration = config.getInt("default-bounty-duration");
    }
}
