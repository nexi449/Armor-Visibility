package com.example.armorvisibility;

import com.example.armorvisibility.compat.ConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Method;

@Mod.EventBusSubscriber(modid = ArmorMod.MOD_ID, value = Dist.CLIENT)
public final class ArmorClient {
    private ArmorClient() {
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if (!(screen instanceof PauseScreen)) {
            return;
        }

        Button statsButton = findStatisticsButton(screen);
        if (statsButton == null) {
            return;
        }

        int iconWidth = 24;
        statsButton.setWidth(statsButton.getWidth() - iconWidth);
        Minecraft client = Minecraft.getInstance();
        Button configButton = new ArmorIconButton(
                statsButton.getX() + statsButton.getWidth() + 4,
                statsButton.getY(),
                button -> {
                    ArmorConfig.INSTANCE.load();
                    showScreen(client, new ConfigScreen(screen));
                });
        event.addListener(configButton);
    }

    private static void showScreen(Minecraft client, Screen screen) {
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
