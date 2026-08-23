package com.example.armorvisibility;

import com.example.armorvisibility.compat.ArmorVisibilityConfigScreen;
import com.mojang.brigadier.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

import static net.minecraft.commands.Commands.literal;

@Mod(value = "armorvisibility", dist = Dist.CLIENT)
public class ArmorVisibilityClient {

    public ArmorVisibilityClient(ModContainer modContainer) {
        ArmorVisibilityConfig.INSTANCE.load();
        modContainer.registerExtensionPoint(IConfigScreenFactory.class,
                (container, parent) -> new ArmorVisibilityConfigScreen(parent));
        NeoForge.EVENT_BUS.addListener(RegisterClientCommandsEvent.class, this::registerCommands);
    }

    public void registerCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(literal("armorvisibility")
                    .executes(context -> {
                        Minecraft client = Minecraft.getInstance();
                        client.execute(() -> {
                            ArmorVisibilityConfig.INSTANCE.load();
                            Screen current = client.screen;
                            client.setScreen(new ArmorVisibilityConfigScreen(current));
                            context.getSource().sendSuccess(() -> Component.literal("Armor Visibility settings opened."), false);
                        });
                        return Command.SINGLE_SUCCESS;
                    }));
    }
}
