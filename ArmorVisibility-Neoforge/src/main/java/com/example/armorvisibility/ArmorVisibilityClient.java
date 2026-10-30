package com.example.armorvisibility;

import com.example.armorvisibility.compat.ArmorVisibilityConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = "armorvisibility", dist = Dist.CLIENT)
public class ArmorVisibilityClient {
    public ArmorVisibilityClient(ModContainer modContainer) {
        ArmorVisibilityConfig.INSTANCE.load();
        modContainer.registerExtensionPoint(IConfigScreenFactory.class,
                (container, parent) -> new ArmorVisibilityConfigScreen(parent));
        NeoForge.EVENT_BUS.addListener(ScreenEvent.Init.Post.class, this::onScreenInit);
        NeoForge.EVENT_BUS.addListener(ScreenEvent.Render.Post.class, this::onScreenRenderPost);
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
        Button configButton = new ArmorVisibilityIconButton(
                statsButton.getX() + statsButton.getWidth() + 4,
                statsButton.getY(),
                button -> {
                    ArmorVisibilityConfig.INSTANCE.load();
                    client.setScreen(new ArmorVisibilityConfigScreen(client.screen));
                });
        event.addListener(configButton);
    }

    private void onScreenRenderPost(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof PauseScreen)) {
            return;
        }

        event.getScreen().children().stream()
                .filter(ArmorVisibilityIconButton.class::isInstance)
                .map(ArmorVisibilityIconButton.class::cast)
                .findFirst()
                .ifPresent(button -> button.renderIcon(event.getGuiGraphics()));
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
