package com.skalpha.potioncounter.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import com.skalpha.potioncounter.PotionCounter;
import net.uku3lig.ukulib.config.screen.PositionSelectScreen;
import net.uku3lig.ukulib.utils.Ukutils;
import org.joml.Vector2ic;

public class DisplayPositionSelectScreen extends PositionSelectScreen {
    private int ticksElapsed = 0;
    // Uh i dont think  i should touch this
    protected DisplayPositionSelectScreen(Screen parent, PotionCounterConfig config) {
        super("Position Select", parent, config.getX(), config.getY(), PotionCounter.getManager(), (x, y) -> {
            config.setX(x);
            config.setY(y);
        });
    }

    @Override
    public void tick() {
        this.ticksElapsed = (this.ticksElapsed + 1) % 100;
        super.tick();
    }

    @Override
    protected void draw(GuiGraphics graphics, int mouseX, int mouseY, float delta, int x, int y) {
        graphics.pose().pushMatrix();
        if (PotionCounter.getManager().getConfig().isUseDefaultPotion()) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, PotionCounter.DEFAULT_POTION, x, y, 0, 0, 32, 32, 16, 16);
        } else {
            graphics.renderItem(PotionCounter.POTION, x, y);
        }

        final Component exampleText = Component.nullToEmpty(String.valueOf(this.ticksElapsed / 4));
        final int color = PotionCounter.getPotionColor(this.ticksElapsed / 10);
        Vector2ic coords = Ukutils.getTextCoords(exampleText, this.width, font, x, y);

        graphics.drawString(this.font, exampleText, coords.x(), coords.y(), color);
        graphics.pose().popMatrix();
    }

    @Override
    protected void drawDefault(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        int x = width / 2 - 8;
        int y = height - 38 - font.lineHeight;
        draw(graphics, mouseX, mouseY, delta, x, y);
    }
}
