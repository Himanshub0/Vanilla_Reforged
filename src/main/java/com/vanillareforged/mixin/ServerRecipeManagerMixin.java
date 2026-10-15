package com.vanillareforged.mixin;

import net.minecraft.recipe.PreparedRecipes;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;
import java.util.Set;

@Mixin(ServerRecipeManager.class)
public abstract class ServerRecipeManagerMixin {

    @Unique
    private static final Set<Identifier> DISABLED_DIAMOND_RECIPES = Set.of(
        Identifier.ofVanilla("diamond_sword"),
        Identifier.ofVanilla("diamond_pickaxe"),
        Identifier.ofVanilla("diamond_axe"),
        Identifier.ofVanilla("diamond_shovel"),
        Identifier.ofVanilla("diamond_hoe"),
        Identifier.ofVanilla("diamond_helmet"),
        Identifier.ofVanilla("diamond_chestplate"),
        Identifier.ofVanilla("diamond_leggings"),
        Identifier.ofVanilla("diamond_boots")
    );

    @ModifyVariable(
        method = "apply(Lnet/minecraft/recipe/PreparedRecipes;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V",
        at = @At("HEAD"),
        argsOnly = true
    )
    private PreparedRecipes filterDirectDiamondEquipmentRecipes(PreparedRecipes preparedRecipes) {
        List<RecipeEntry<?>> filtered = preparedRecipes.recipes().stream()
            .filter(entry -> !DISABLED_DIAMOND_RECIPES.contains(entry.id().getValue()))
            .toList();
        return PreparedRecipes.of(filtered);
    }
}
