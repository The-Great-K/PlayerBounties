package com.fadingdaze.playerBounties.config;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import com.fadingdaze.playerBounties.PlayerBounties;

public class BountyPermRestrictConfig {
	private static final BountyPermRestrictConfig INSTANCE = new BountyPermRestrictConfig();

	private File file;
	private YamlConfiguration config;

	public static HashSet<String> restrictedPerms = new HashSet<>();

	public static BountyPermRestrictConfig getInstance() {
		return INSTANCE;
	}

	@SuppressWarnings("unchecked")
	public void load() {
		file = new File(PlayerBounties.getInstance().getDataFolder(), "permrestrict.yml");

		if (!file.exists()) {
			PlayerBounties.getInstance().saveResource("permrestrict.yml", false);
		}

		config = new YamlConfiguration();
		try {
			config.load(file);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}

		restrictedPerms.addAll((List<String>) config.getList("restricted"));
	}
}
