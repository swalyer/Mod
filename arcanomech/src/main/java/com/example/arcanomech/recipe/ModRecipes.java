package com.example.arcanomech.recipe;

import com.example.arcanomech.Arcanomech;
import com.example.arcanomech.altar.AltarRecipe;
import com.example.arcanomech.altar.AltarRecipeSerializer;
import com.example.arcanomech.recipe.WorkbenchRecipe;
import com.example.arcanomech.recipe.WorkbenchRecipeSerializer;

import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModRecipes {
    public static final RecipeType<CrusherRecipe> CRUSHER_RECIPE_TYPE = RecipeType.register(Arcanomech.id("crusher"));
    public static final RecipeSerializer<CrusherRecipe> CRUSHER_SERIALIZER = Registry.register(
            Registries.RECIPE_SERIALIZER,
            new Identifier(Arcanomech.MOD_ID, "crusher"),
            new CrusherRecipeSerializer()
    );

    public static final RecipeType<WorkbenchRecipe> WORKBENCH_RECIPE_TYPE = RecipeType.register(Arcanomech.id("workbench"));
    public static final RecipeSerializer<WorkbenchRecipe> WORKBENCH_SERIALIZER = Registry.register(
            Registries.RECIPE_SERIALIZER,
            new Identifier(Arcanomech.MOD_ID, "workbench"),
            new WorkbenchRecipeSerializer()
    );

    public static final RecipeType<AltarRecipe> ALTAR_RECIPE_TYPE = RecipeType.register(Arcanomech.id("altar"));
    public static final RecipeSerializer<AltarRecipe> ALTAR_SERIALIZER = Registry.register(
            Registries.RECIPE_SERIALIZER,
            new Identifier(Arcanomech.MOD_ID, "altar"),
            new AltarRecipeSerializer()
    );

    private ModRecipes() {
    }

    public static void registerAll() {
        // static initializers handle registration
    }
}
