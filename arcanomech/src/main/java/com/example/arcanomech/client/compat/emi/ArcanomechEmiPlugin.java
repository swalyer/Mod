package com.example.arcanomech.client.compat.emi;

import com.example.arcanomech.Arcanomech;
import com.example.arcanomech.ModContent;
import com.example.arcanomech.recipe.CrusherRecipe;
import com.example.arcanomech.recipe.ModRecipes;
import com.example.arcanomech.recipe.WorkbenchRecipe;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ArcanomechEmiPlugin implements EmiPlugin {
    private static final Identifier WORKBENCH_ID = new Identifier(Arcanomech.MOD_ID, "workbench");
    private static final Identifier CRUSHER_ID = new Identifier(Arcanomech.MOD_ID, "crushing");
    private static final EmiRecipeCategory WORKBENCH_CATEGORY = new EmiRecipeCategory(WORKBENCH_ID, EmiStack.of(ModContent.ARCANE_WORKBENCH));
    private static final EmiRecipeCategory CRUSHER_CATEGORY = new EmiRecipeCategory(CRUSHER_ID, EmiStack.of(ModContent.CRUSHER));

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(WORKBENCH_CATEGORY);
        registry.addWorkstation(WORKBENCH_CATEGORY, EmiStack.of(ModContent.ARCANE_WORKBENCH));
        registry.getRecipeManager().listAllOfType(ModRecipes.WORKBENCH_RECIPE_TYPE).forEach(recipe -> registry.addRecipe(new WorkbenchEmiRecipe(WORKBENCH_CATEGORY, recipe)));

        registry.addCategory(CRUSHER_CATEGORY);
        registry.addWorkstation(CRUSHER_CATEGORY, EmiStack.of(ModContent.CRUSHER));
        registry.getRecipeManager().listAllOfType(ModRecipes.CRUSHER_RECIPE_TYPE).forEach(recipe -> registry.addRecipe(new CrusherEmiRecipe(CRUSHER_CATEGORY, recipe)));
    }
}
