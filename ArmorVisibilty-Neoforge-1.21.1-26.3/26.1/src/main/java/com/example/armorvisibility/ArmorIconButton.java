package com.example.armorvisibility;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class ArmorIconButton extends Button {
    private static final Identifier ARMOR_ICON = Identifier.fromNamespaceAndPath(
            "armorvisibility", "textures/gui/armoricon.png");

    public ArmorIconButton(int x, int y, OnPress onPress) {
        super(x, y, 20, 20, Component.empty(), onPress, DEFAULT_NARRATION);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        extractDefaultSprite(graphics);
        renderIcon(graphics);
    }

    void renderIcon(GuiGraphicsExtractor graphics) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, ARMOR_ICON, getX() + 2, getY() + 2,
                0, 0, 16, 16, 16, 16);
    }
}
