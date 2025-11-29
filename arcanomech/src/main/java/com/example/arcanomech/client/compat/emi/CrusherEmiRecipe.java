package com.example.arcanomech.client.compat.emi;

import java.util.List;

import com.example.arcanomech.recipe.CrusherRecipe;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class CrusherEmiRecipe implements EmiRecipe {
    private static final int WIDTH = 100;
    private static final int HEIGHT = 40;

    private final EmiRecipeCategory category;
    private final CrusherRecipe recipe;

    public CrusherEmiRecipe(EmiRecipeCategory category, CrusherRecipe recipe) {
        this.category = category;
        this.recipe = recipe;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public Identifier getId() {
        return recipe.getId();
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(EmiIngredient.of(recipe.getIngredient()));
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(EmiStack.of(recipe.getResult()));
    }

    @Override
    public int getDisplayWidth() {
        return WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return HEIGHT;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(getInputs().get(0), 4, 10);
        widgets.addSlot(getOutputs().get(0), 60, 6).large(true);
        widgets.addText(net.minecraft.text.Text.literal("Time: " + recipe.getWorkTime() + "t"), 4, 30, 0x404040, false);
    }
}
