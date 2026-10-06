package com.example.armorvisibility;

import com.example.armorvisibility.compat.ArmorVisibilityConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
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
        NeoForge.EVENT_BUS.addListener(ScreenEvent.Render.Post.class, this::onScreenRender);
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
        Button configButton = Button.builder(Component.empty(), button -> {
            ArmorVisibilityConfig.INSTANCE.load();
            client.setScreen(new ArmorVisibilityConfigScreen(client.screen));
        }).bounds(statsButton.getX() + statsButton.getWidth() + 4, statsButton.getY(), 20, 20).build();
        event.addListener(configButton);
    }

    private void onScreenRender(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof PauseScreen)) {
            return;
        }

        Button statsButton = findStatisticsButton(event.getScreen());
        if (statsButton == null) {
            return;
        }

        int configButtonX = statsButton.getX() + statsButton.getWidth() + 4;
        int configButtonY = statsButton.getY();
        event.getScreen().children().stream()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .filter(button -> button.getX() == configButtonX && button.getY() == configButtonY)
                .findFirst()
                .ifPresent(button -> event.getGuiGraphics().blit(RenderPipelines.GUI_TEXTURED, ARMOR_ICON,
                        button.getX() + 2, button.getY() + 2, 0, 0, 16, 16, 16, 16));
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
