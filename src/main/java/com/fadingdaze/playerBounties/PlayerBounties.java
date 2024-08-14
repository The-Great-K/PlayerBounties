package com.fadingdaze.playerBounties;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

import com.fadingdaze.playerBounties.commands.BountyCommand;
import com.fadingdaze.playerBounties.commands.GetTrackerCommand;
import com.fadingdaze.playerBounties.commands.GiveBountyActivatorCommand;
import com.fadingdaze.playerBounties.config.BountyPermRestrictConfig;
import com.fadingdaze.playerBounties.config.BountyRewardConfig;
import com.fadingdaze.playerBounties.keys.Keys;
import com.fadingdaze.playerBounties.recipes.PluginRecipes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

public final class PlayerBounties extends JavaPlugin implements Listener {
	public HashSet<Player> hasTracker = new HashSet<>();
	public HashMap<UUID, PermissionAttachment> perms = new HashMap<>();

	private Player bountyHead = null;
	private int bountyDuration = 10800; // default value, 10800 seconds / 3 hours

	private Player lastBountyDamager;
	private int damageTimer = 60;

	private final Random rand = new Random();

	private final Runnable tickFunction = () -> {
		/* ---------------------- BOUNTY MANAGEMENT ---------------------- */
		if (getBountyDuration() <= 0) {
			Bukkit.broadcast(Component.text("Bounty has ended! Nobody got the bounty!", NamedTextColor.DARK_RED));
			endBounty(getBounty());
		}

		if (getBounty() != null) {
			bountyDuration--;
		}

		if (getLastBountyDamager() != null) {
			if (getDamageTimer() <= 0) {
				setDamageTimer(60);
				setLastBountyDamager(null);
			}
			damageTimer--;
		}

		/* ---------------------- TRACKING COMPASS ---------------------- */
		Bukkit.getOnlinePlayers().forEach(player -> {
			for (ItemStack item : player.getInventory().getContents()) {
				if (item != null && item.getType() == Material.COMPASS
						&& item.getItemMeta().getPersistentDataContainer().has(Keys.TRACKING_COMPASS)) {
					CompassMeta cMeta = (CompassMeta) item.getItemMeta();
					cMeta.setLodestoneTracked(false);
					cMeta.lore(List.of(Component.text(formatSeconds(getBountyDuration()) + " left in bounty")));

					if (getBounty() != null) {
						if (player.getWorld().equals(getBounty().getWorld())) {
							cMeta.setLodestone(getBounty().getLocation());
							cMeta.displayName(Component.text("Tracking: ", NamedTextColor.BLUE)
									.append(Component.text(getBounty().getName(), NamedTextColor.RED)));

							item.setItemMeta(cMeta);
						} else {
							cMeta.displayName(
									Component.text("Bounty in different dimension: " + getBounty().getWorld().getName(),
											NamedTextColor.DARK_RED));

							item.setItemMeta(cMeta);
						}
					} else {
						item.subtract();
						player.sendMessage("Bounty has ended! Removed tracking compass.");
						player.sendMessage("You may use /gettracker again when another bounty starts.");
					}
				}
			}
		});
	};

	public static PlayerBounties getInstance() {
		return getPlugin(PlayerBounties.class);
	}

	public void endBounty(@Nullable Player winner) {
		if (getBounty() != null)
			removeBountyPermRestrictions(getBounty());
		List<String> commands = BountyRewardConfig.rewards.get(rand.nextInt(BountyRewardConfig.rewards.size()));

		Bukkit.getOnlinePlayers().forEach(
				player -> player.showTitle(Title.title(Component.text("BOUNTY ENDED!", NamedTextColor.DARK_RED),
						Component.text(getBounty().getName(), NamedTextColor.RED))));

		if (winner != null) {
			for (String command : commands) {
				command = command.replaceAll("%player%", winner.getName());
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
			}
		}

		setBountyDuration(10800);
		setBounty(null);
		hasTracker.clear();
	}

	@Override
	public void onEnable() {
		// Plugin startup logic
		Bukkit.getPluginManager().registerEvents(this, this);

		BountyRewardConfig.getInstance().load();
		getLogger().info("REWARDS DEBUG:");
		for (List<String> list : BountyRewardConfig.rewards) {
			getLogger().info(list.toString());
		}
		BountyPermRestrictConfig.getInstance().load();
		getLogger().info("RESTRICTIONS DEBUG:");
		getLogger().info(BountyPermRestrictConfig.restrictedPerms.toString());

		PluginRecipes.register();

		getCommand("bounty").setExecutor(new BountyCommand(this));
		getCommand("gettracker").setExecutor(new GetTrackerCommand(this));
		getCommand("givebountyactivator").setExecutor(new GiveBountyActivatorCommand(this));

		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, tickFunction, 0, 20); // runs every second (20 ticks)

		getLogger().info("""
				\s
				______  _                           ______                       _    _           \s
				| ___ \\| |                          | ___ \\                     | |  (_)          \s
				| |_/ /| |  __ _  _   _   ___  _ __ | |_/ /  ___   _   _  _ __  | |_  _   ___  ___\s
				|  __/ | | / _` || | | | / _ \\| '__|| ___ \\ / _ \\ | | | || '_ \\ | __|| | / _ \\/ __|
				| |    | || (_| || |_| ||  __/| |   | |_/ /| (_) || |_| || | | || |_ | ||  __/\\__ \\
				\\_|    |_| \\__,_| \\__, | \\___||_|   \\____/  \\___/  \\__,_||_| |_| \\__||_| \\___||___/
				                   __/ |                                                          \s
				                  |___/         \s""");

		getLogger().info("Player Bounties plugin successfully loaded!");
	}

	@Override
	public void onDisable() {
		// Plugin shutdown logic
		endBounty(null);
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		if (event.getPlayer().equals(getBounty())) {
			getLogger().log(Level.INFO, "Player with bounty has left!");
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (event.getPlayer() == getBounty()) {
			if (getLastBountyDamager() != null) {
				Bukkit.broadcast(Component.text("Bounty, " + getBounty().getName() + " has been killed by "
						+ getLastBountyDamager().getName() + "!", NamedTextColor.DARK_RED));
				endBounty(getLastBountyDamager());
			}
		}
	}

	@EventHandler
	public void onPlayerDamage(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player player) {
			setLastBountyDamager(player);
			setDamageTimer(60);
		}
	}

	@EventHandler
	public void onClickEvent(PlayerInteractEvent event) {
		if (veryLongVerboseIfStatement(event, event.getPlayer())) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "bounty set " + event.getPlayer().getName());
			event.getPlayer().getInventory().getItemInMainHand().subtract();
		} else if (anotherVeryLongVerboseIfStatement(event, event.getPlayer())) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "bounty set " + event.getPlayer().getName());
			event.getPlayer().getInventory().getItemInOffHand().subtract();
		}
	}

	public Player getBounty() {
		return this.bountyHead;
	}

	public void setBounty(@Nullable Player player) {
		this.bountyHead = player;
	}

	public int getBountyDuration() {
		return this.bountyDuration;
	}

	public void setBountyDuration(int bountyDuration) {
		this.bountyDuration = bountyDuration;
	}

	public Player getLastBountyDamager() {
		return lastBountyDamager;
	}

	public void setLastBountyDamager(@Nullable Player player) {
		this.lastBountyDamager = player;
	}

	public int getDamageTimer() {
		return damageTimer;
	}

	public void setDamageTimer(int seconds) {
		this.damageTimer = seconds;
	}

	public void setBountyPermRestrictions(Player player) {
		PermissionAttachment attachment = player.addAttachment(this);
		perms.put(player.getUniqueId(), attachment);

		BountyPermRestrictConfig.restrictedPerms.forEach(perm -> {
			PermissionAttachment permissionAttachment = perms.get(player.getUniqueId());
			permissionAttachment.setPermission(perm, true);
			getLogger().info("Set permission: " + perm);
		});
	}

	public void removeBountyPermRestrictions(Player player) {
		if (player.getUniqueId().equals(getBounty().getUniqueId())) {
			getLogger().info("UUID's match!");
		}
		BountyPermRestrictConfig.restrictedPerms.forEach(perm -> {
			perms.get(player.getUniqueId()).unsetPermission(perm);
			getLogger().info("Unset permission: " + perm);
		});

		perms.remove(player.getUniqueId());
	}

	public String formatSeconds(int seconds) {
		int minutes = seconds / 60;
		seconds %= 60;
		int hours = minutes / 60;
		minutes %= 60;

		if (hours <= 0) {
			if (minutes <= 0) {
				return seconds + "s";
			} else {
				return minutes + "m " + seconds + "s";
			}
		} else {
			return hours + "h " + minutes + "m " + seconds + "s";
		}
	}

	private boolean veryLongVerboseIfStatement(PlayerInteractEvent e, Player player) {
		return player.getInventory().getItemInMainHand().getType().equals(Material.NETHER_STAR)
				&& player.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer()
						.has(Keys.BOUNTY_ACTIVATOR)
				&& (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK));
	}

	private boolean anotherVeryLongVerboseIfStatement(PlayerInteractEvent e, Player player) {
		return player.getInventory().getItemInOffHand().getType().equals(Material.NETHER_STAR)
				&& player.getInventory().getItemInOffHand().getItemMeta().getPersistentDataContainer()
						.has(Keys.BOUNTY_ACTIVATOR)
				&& (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK));
	}
}