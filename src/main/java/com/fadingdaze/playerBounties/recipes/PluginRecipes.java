package com.fadingdaze.playerBounties.recipes;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapelessRecipe;

import com.fadingdaze.playerBounties.PlayerBounties;
import com.fadingdaze.playerBounties.commands.GiveBountyActivatorCommand;

public class PluginRecipes {
	public static void register() {
		ShapelessRecipe baRecipe = new ShapelessRecipe(
				new NamespacedKey(PlayerBounties.getInstance(), "BountyActivatorRecipe"),
				GiveBountyActivatorCommand.getBountyActivator());
		baRecipe.addIngredient(1, Material.NETHER_STAR);
		baRecipe.addIngredient(1, Material.END_CRYSTAL);

		Bukkit.addRecipe(baRecipe);
	}
}
