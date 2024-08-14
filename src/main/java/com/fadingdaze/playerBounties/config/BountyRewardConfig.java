package com.fadingdaze.playerBounties.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import com.fadingdaze.playerBounties.PlayerBounties;

public class BountyRewardConfig {
	private final static BountyRewardConfig instance = new BountyRewardConfig();
	public static ArrayList<List<String>> rewards = new ArrayList<>();
	private File file;
	private YamlConfiguration config;

	public static BountyRewardConfig getInstance() {
		return instance;
	}

	@SuppressWarnings("unchecked")
	public void load() {
		file = new File(PlayerBounties.getInstance().getDataFolder(), "rewards.yml");

		if (!file.exists()) {
			PlayerBounties.getInstance().saveResource("rewards.yml", false);
		}

		config = new YamlConfiguration();
		try {
			config.load(file);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}

		config.getConfigurationSection("rewards").getKeys(false).forEach(index -> {
			rewards.add((List<String>) config.getList("rewards." + index + ".reward"));
		});
	}
}
