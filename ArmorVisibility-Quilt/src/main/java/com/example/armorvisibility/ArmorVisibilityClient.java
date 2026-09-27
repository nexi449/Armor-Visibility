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
import net.minecraft.client.gui.tooltip.Tooltip;
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

            ButtonWidget configButton = new ArmorIconButton(
                    statsButton.getX() + statsButton.getWidth() + 4, statsButton.getY(), button -> {
                ArmorVisibilityConfig.INSTANCE.load();
                client.setScreen(new ArmorVisibilityConfigScreen(client.currentScreen));
            });
            Screens.getButtons(screen).add(configButton);
        });

    }

    private static final class ArmorIconButton extends ButtonWidget {
        private ArmorIconButton(int x, int y, PressAction onPress) {
            super(x, y, 20, 20, net.minecraft.text.Text.of(""), onPress, DEFAULT_NARRATION_SUPPLIER);
            setTooltip(Tooltip.of(net.minecraft.text.Text.of("Armor Visibility")));
        }

        @Override
        protected void drawIcon(DrawContext context, int mouseX, int mouseY, float delta) {
            drawButton(context);
            context.drawTexture(RenderPipelines.GUI_TEXTURED, ARMOR_ICON, getX() + 2, getY() + 2,
                    0, 0, 16, 16, 16, 16);
        }
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
