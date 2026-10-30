package com.example.armorvisibility;

import com.example.armorvisibility.compat.ArmorVisibilityConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.lang.reflect.Method;

@EventBusSubscriber(modid = ArmorVisibilityMod.MOD_ID, value = Dist.CLIENT)
public final class ArmorVisibilityClient {
    private ArmorVisibilityClient() {
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if (!(screen instanceof PauseScreen)) {
            return;
        }

        String statsLabel = Component.translatable("gui.stats").getString();
        AbstractWidget statsButton = screen.children().stream()
                .filter(AbstractWidget.class::isInstance)
                .map(AbstractWidget.class::cast)
                .filter(widget -> widget.getMessage().getString().equals(statsLabel))
                .findFirst()
                .orElse(null);
        if (statsButton == null) {
            return;
        }

        Button configButton = new ArmorVisibilityIconButton(
                statsButton.getX() + statsButton.getWidth() + 4,
                statsButton.getY(),
                button -> {
            ArmorVisibilityConfig.INSTANCE.load();
            showScreen(Minecraft.getInstance(), new ArmorVisibilityConfigScreen(screen));
        });
        event.addListener(configButton);
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
}
