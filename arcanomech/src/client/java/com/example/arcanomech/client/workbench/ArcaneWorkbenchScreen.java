package com.example.arcanomech.client.workbench;

import com.example.arcanomech.Arcanomech;

import com.example.arcanomech.workbench.ArcaneWorkbenchScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ArcaneWorkbenchScreen extends HandledScreen<ArcaneWorkbenchScreenHandler> {
    private static final Identifier TEXTURE = new Identifier(Arcanomech.MOD_ID, "textures/gui/arcane_workbench.png");
    private static final int PROGRESS_U = 176;
    private static final int PROGRESS_V = 0;
    private static final int PROGRESS_WIDTH = 24;
    private static final int PROGRESS_HEIGHT = 17;
    private static final int MANA_BAR_U = 176;
    private static final int MANA_BAR_V = 17;
    private static final int MANA_BAR_WIDTH = 12;
    private static final int MANA_BAR_HEIGHT = 52;

    public ArcaneWorkbenchScreen(ArcaneWorkbenchScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        backgroundWidth = 176;
        backgroundHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);
        int workTime = handler.getWorkTime();
        if (workTime > 0) {
            int progress = handler.getProgress();
            int width = progress * PROGRESS_WIDTH / workTime;
            if (width > 0) {
                context.drawTexture(TEXTURE, x + 79, y + 35, PROGRESS_U, PROGRESS_V, width, PROGRESS_HEIGHT);
            }
        }
        int manaCost = handler.getManaCost();
        if (manaCost > 0) {
            int mana = Math.min(handler.getMana(), manaCost);
            int filled = mana * MANA_BAR_HEIGHT / manaCost;
            if (filled > 0) {
                context.drawTexture(TEXTURE, x + 150, y + 10 + (MANA_BAR_HEIGHT - filled), MANA_BAR_U,
                        MANA_BAR_V + (MANA_BAR_HEIGHT - filled), MANA_BAR_WIDTH, filled);
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
        drawManaTooltip(context, mouseX, mouseY);
    }

    private void drawManaTooltip(DrawContext context, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        int barX = x + 150;
        int barY = y + 10;
        if (mouseX >= barX && mouseX <= barX + MANA_BAR_WIDTH && mouseY >= barY && mouseY <= barY + MANA_BAR_HEIGHT) {
            int mana = handler.getMana();
            int manaCost = handler.getManaCost();
            context.drawTooltip(textRenderer,
                    Text.translatable("tooltip." + Arcanomech.MOD_ID + ".mana_cost", mana, manaCost, handler.getWorkTime()),
                    mouseX, mouseY);
        }
    }
}
