package com.fadingdaze.playerBounties.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.fadingdaze.playerBounties.PlayerBounties;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

public class BountyCommand implements CommandExecutor, TabExecutor {
	private final PlayerBounties plugin;

	public BountyCommand(PlayerBounties plugin) {
		this.plugin = plugin;
	}

	// command only available to admins, unless player has perm
	// [playerbounties.command.bounty] (see plugin.yml)
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String name,
			@NotNull String[] args) {
		if (args.length == 0) { // if no parameters are given
			sender.sendMessage("ERROR: Please include parameters!");
			return false;
		}
		if (args.length >= 1 && args[0].equalsIgnoreCase("clear")) { // clear bounty when [/bounty clear] is used
			if (plugin.getBounty() != null) { // if bounty exists, clear bounty
				sender.sendMessage(plugin.getBounty().getName() + "'s bounty cleared!");
				Bukkit.broadcast(
						Component.text(plugin.getBounty().getName() + "'s bounty cleared!", NamedTextColor.DARK_RED));
				plugin.endBounty(null);
				return true;
			} else { // otherwise send error
				sender.sendMessage("ERROR: Cannot clear non-existent bounty!");
				return false;
			}
		} else if (args[0].equalsIgnoreCase("set")) {
			if (plugin.getBounty() != null) { // if player bounty already exists
				sender.sendMessage("ERROR: Player bounty already exists!"); // throws error at player
				sender.sendMessage("Use /bounty clear, to clear bounty.");
				return false;
			}

			if (args.length >= 2 && !args[1].isBlank()) {
				Bukkit.getOnlinePlayers().forEach(player -> {
					if (player.getName().equalsIgnoreCase(args[1])) { // if player exists, start bounty for player
						plugin.setBounty(player);

						plugin.getLogger().log(Level.INFO, "Bounty started for " + player.getName());
					}
				});
				if (plugin.getBounty() == null) { // if no bounty has been created yet, throw error
					sender.sendMessage("ERROR: Invalid player name!");
					plugin.getLogger().log(Level.WARNING, "Attempted to start bounty on non-existent/offline player!");
					return false;
				} else {
					if (args.length >= 3 && !args[2].isBlank()) {
						try {
							plugin.setBountyDuration(Integer.parseInt(args[2]));
						} catch (NumberFormatException e) {
							sender.sendMessage("ERROR: 3rd parameter must be an integer duration in seconds!");
							return false;
						}
					} else {
						plugin.setBountyDuration(10800);
					}
				}
			} else {
				sender.sendMessage("ERROR: Please include a player name!");
				return false;
			}
		}
		plugin.setBountyPermRestrictions(plugin.getBounty());
		plugin.getLogger().info(plugin.getBounty().getUniqueId().toString());
		Bukkit.getOnlinePlayers().forEach(
				player -> player.showTitle(Title.title(Component.text("BOUNTY STARTED!", NamedTextColor.DARK_RED),
						Component.text(plugin.getBounty().getName(), NamedTextColor.RED))));
		Bukkit.broadcast(Component
				.text("Bounty started for " + plugin.getBounty().getName() + "! " + "You have "
						+ plugin.formatSeconds(plugin.getBountyDuration())
						+ " to hunt them down. Use /gettracker to get a tracking compass.", NamedTextColor.DARK_RED)
				.append(Component.text("\nRemember: you can only claim one compass per bounty!", NamedTextColor.RED)));

		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command,
			@NotNull String s, @NotNull String[] strings) {
		return new ArrayList<>(); // returns list of all players online
	}
}
