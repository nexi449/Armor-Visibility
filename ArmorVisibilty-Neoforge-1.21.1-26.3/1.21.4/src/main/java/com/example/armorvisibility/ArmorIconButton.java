package com.example.armorvisibility;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class ArmorIconButton extends Button {
    private static final ResourceLocation ARMOR_ICON = ResourceLocation.fromNamespaceAndPath(
            "armorvisibility", "textures/gui/armoricon.png");

    public ArmorIconButton(int x, int y, OnPress onPress) {
        super(x, y, 20, 20, Component.empty(), onPress, DEFAULT_NARRATION);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.renderWidget(graphics, mouseX, mouseY, delta);
        renderIcon(graphics);
    }

    void renderIcon(GuiGraphics graphics) {
        graphics.blit(RenderType::guiTextured, ARMOR_ICON, getX() + 2, getY() + 2,
                0.0F, 0.0F, 16, 16, 16, 16);
    }
}
