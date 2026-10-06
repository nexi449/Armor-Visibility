package com.example.armorvisibility;

import com.example.armorvisibility.compat.ArmorVisibilityConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.lang.reflect.Method;

public class ArmorVisibilityClient implements ClientModInitializer {
    private static final Identifier ARMOR_ICON = Identifier.fromNamespaceAndPath(
            "armorvisibility", "textures/gui/armoricon.png");

    @Override
    public void onInitializeClient() {
        ArmorVisibilityConfig.INSTANCE.load();

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof PauseScreen)) {
                return;
            }

            AbstractWidget statsButton = findStatisticsButton(screen);
            if (statsButton == null) {
                return;
            }

            Button configButton = Button.builder(Component.empty(), button -> {
                ArmorVisibilityConfig.INSTANCE.load();
                showScreen(client, new ArmorVisibilityConfigScreen(screen));
            }).bounds(statsButton.getX() + statsButton.getWidth() + 4, statsButton.getY(), 20, 20).build();
            Screens.getWidgets(screen).add(configButton);

            ScreenEvents.afterForeground(screen).register((currentScreen, extractor, mouseX, mouseY, delta) ->
                    extractor.blit(RenderPipelines.GUI_TEXTURED, ARMOR_ICON, configButton.getX() + 2,
                            configButton.getY() + 2, 0, 0, 16, 16, 16, 16));
        });

    }

    public static void showScreen(Minecraft client, Screen screen) {
        try {
            Method method;
            try {
                method = Minecraft.class.getMethod("setScreenAndShow", Screen.class);
            } catch (NoSuchMethodException ignored) {
                method = Minecraft.class.getMethod("setScreen", Screen.class);
            }
            method.invoke(client, screen);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to open Armor Visibility screen", exception);
        }
    }

    private static AbstractWidget findStatisticsButton(Screen screen) {
        String statsLabel = Component.translatable("gui.stats").getString();
        return Screens.getWidgets(screen).stream()
                .filter(button -> button.getMessage().getString().equals(statsLabel))
                .findFirst()
                .orElse(null);
    }
}
