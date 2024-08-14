package com.fadingdaze.playerBounties.commands;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import com.fadingdaze.playerBounties.PlayerBounties;
import com.fadingdaze.playerBounties.keys.Keys;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class GiveBountyActivatorCommand implements CommandExecutor {
	private final PlayerBounties plugin;

	public GiveBountyActivatorCommand(PlayerBounties plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String name,
			@NotNull String[] args) {
		if (args.length >= 1) {
			AtomicBoolean givenActivator = new AtomicBoolean(false);

			Bukkit.getOnlinePlayers().forEach(player -> {
				if (player.getName().equalsIgnoreCase(args[0])) {
					player.getInventory().addItem(getBountyActivator());

					givenActivator.set(true);
				}
			});

			if (givenActivator.get()) {
				return true;
			} else {
				sender.sendMessage("ERROR: Invalid player name!");
				return false;
			}
		}

		if (sender instanceof Player player) {
			player.getInventory().addItem(getBountyActivator());

			plugin.getLogger().log(Level.INFO, "Gave " + player.getName() + " 1 Bounty Activator");

			return true;
		} else {
			sender.sendMessage("Please include a player name!");

			return false;
		}
	}

	public static ItemStack getBountyActivator() {
		ItemStack bountyActivator = new ItemStack(Material.NETHER_STAR);
		ItemMeta meta = bountyActivator.getItemMeta();

		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		meta.addEnchant(Enchantment.UNBREAKING, 3, true);

		meta.displayName(Component.text("Bounty Activator", NamedTextColor.DARK_PURPLE));
		meta.lore(List.of(Component.text("Right click this item to start a bounty on yourself.")));

		meta.getPersistentDataContainer().set(Keys.BOUNTY_ACTIVATOR, PersistentDataType.BOOLEAN, true);

		bountyActivator.setItemMeta(meta);

		return bountyActivator;
	}
}
