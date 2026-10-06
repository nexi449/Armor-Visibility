package com.example.armorvisibility;

import com.example.armorvisibility.compat.ArmorVisibilityConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ArmorVisibilityClient implements ClientModInitializer {
    private static final Identifier ARMOR_ICON = Identifier.of("armorvisibility", "textures/gui/armoricon.png");

    @Override
    public void onInitializeClient() {
        ArmorVisibilityConfig.INSTANCE.load();

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof GameMenuScreen)) {
                return;
            }

            ClickableWidget statsButton = findStatisticsButton(screen);
            if (statsButton == null) {
                return;
            }

            ButtonWidget configButton = ButtonWidget.builder(net.minecraft.text.Text.of(""), button -> {
                ArmorVisibilityConfig.INSTANCE.load();
                client.setScreen(new ArmorVisibilityConfigScreen(client.currentScreen));
            }).dimensions(statsButton.getX() + statsButton.getWidth() + 4, statsButton.getY(), 20, 20).build();
            Screens.getButtons(screen).add(configButton);
        });

    }

    public static void renderPauseMenuIcon(Screen screen, DrawContext context) {
        if (!(screen instanceof GameMenuScreen)) {
            return;
        }

        ClickableWidget statsButton = findStatisticsButton(screen);
        if (statsButton == null) {
            return;
        }

        context.drawTexture(RenderPipelines.GUI_TEXTURED, ARMOR_ICON,
                statsButton.getX() + statsButton.getWidth() + 6, statsButton.getY() + 2,
                0, 0, 16, 16, 16, 16);
    }

    private static ClickableWidget findStatisticsButton(Screen screen) {
        String statsLabel = Text.translatable("gui.stats").getString();
        String alternateStatsLabel = Text.translatable("menu.stats").getString();
        return Screens.getButtons(screen).stream()
                .filter(button -> {
                    String text = button.getMessage().getString();
                    return text.equals(statsLabel) || text.equals(alternateStatsLabel) || text.equals("Statistics");
                })
                .findFirst()
                .orElse(null);
    }
}
