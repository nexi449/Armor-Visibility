package com.example.armorvisibility;

import com.example.armorvisibility.compat.ArmorVisibilityConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = "armorvisibility", dist = Dist.CLIENT)
public class ArmorVisibilityClient {
        private static final Identifier ARMOR_ICON = Identifier.fromNamespaceAndPath(
            "armorvisibility", "textures/gui/armoricon.png");

    public ArmorVisibilityClient(ModContainer modContainer) {
        ArmorVisibilityConfig.INSTANCE.load();
        modContainer.registerExtensionPoint(IConfigScreenFactory.class,
                (container, parent) -> new ArmorVisibilityConfigScreen(parent));
        NeoForge.EVENT_BUS.addListener(ScreenEvent.Init.Post.class, this::onScreenInit);
    }

    private void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if (!(screen instanceof PauseScreen)) {
            return;
        }

        Button statsButton = findStatisticsButton(screen);
        if (statsButton == null) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        Button configButton = new ArmorIconButton(
                statsButton.getX() + statsButton.getWidth() + 4, statsButton.getY(), button -> {
            ArmorVisibilityConfig.INSTANCE.load();
            client.setScreen(new ArmorVisibilityConfigScreen(client.screen));
        });
        event.addListener(configButton);
    }

    private static final class ArmorIconButton extends Button {
        private ArmorIconButton(int x, int y, OnPress onPress) {
            super(x, y, 20, 20, Component.empty(), onPress, DEFAULT_NARRATION);
            setTooltip(Tooltip.create(Component.literal("Armor Visibility")));
        }

        @Override
        protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            renderDefaultSprite(graphics);
            graphics.blit(RenderPipelines.GUI_TEXTURED, ARMOR_ICON, getX() + 2, getY() + 2,
                    0, 0, 16, 16, 16, 16);
        }
    }

    private static Button findStatisticsButton(Screen screen) {
        String statsLabel = Component.translatable("gui.stats").getString();
        return screen.children().stream()
                .filter(AbstractWidget.class::isInstance)
                .map(AbstractWidget.class::cast)
                .filter(widget -> widget.getMessage().getString().equals(statsLabel))
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .findFirst()
                .orElse(null);
    }

}
