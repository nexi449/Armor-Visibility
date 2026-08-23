package com.example.armorvisibility;

import com.example.armorvisibility.compat.ArmorVisibilityConfigScreen;
import com.mojang.brigadier.Command;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ArmorVisibilityClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ArmorVisibilityConfig.INSTANCE.load();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("armorvisibility")
                    .executes(context -> {
                        MinecraftClient client = MinecraftClient.getInstance();
                        client.execute(() -> {
                            ArmorVisibilityConfig.INSTANCE.load();
                            Screen current = client.currentScreen;
                            client.setScreen(new ArmorVisibilityConfigScreen(current));
                            context.getSource().sendFeedback(Text.literal("Armor Visibility settings opened."));
                        });
                        return Command.SINGLE_SUCCESS;
                    }));
        });
    }
}
