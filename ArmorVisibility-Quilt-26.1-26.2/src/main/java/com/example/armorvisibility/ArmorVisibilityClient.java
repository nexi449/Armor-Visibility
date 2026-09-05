package com.example.armorvisibility;

import com.example.armorvisibility.compat.ArmorVisibilityConfigScreen;
import com.mojang.brigadier.Command;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.lang.reflect.Method;

public class ArmorVisibilityClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ArmorVisibilityConfig.INSTANCE.load();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal("armorvisibility")
                    .executes(context -> {
                        Minecraft client = Minecraft.getInstance();
                        client.execute(() -> {
                            ArmorVisibilityConfig.INSTANCE.load();
                            showScreen(client, new ArmorVisibilityConfigScreen(null));
                            context.getSource().sendFeedback(Component.literal("Armor Visibility settings opened."));
                        });
                        return Command.SINGLE_SUCCESS;
                    }));
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
}
