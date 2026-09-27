package com.example.armorvisibility.compat;

import com.example.armorvisibility.ArmorVisibilityConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ArmorVisibilityConfigScreen extends Screen {
    private final Screen parent;

    public ArmorVisibilityConfigScreen(Screen parent) {
        super(Component.literal("Armor Visibility"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        ArmorVisibilityConfig.INSTANCE.load();

        int contentWidth = Math.min(368, Math.max(0, width - 32));
        int buttonWidth = (contentWidth - 8) / 2;
        int leftX = (width - contentWidth) / 2;
        int rightX = leftX + buttonWidth + 8;
        int startY = Math.max(48, (height - 140) / 2);

        addArmorToggle("Helmet", ArmorVisibilityConfig.ArmorPart.HELMET, leftX, startY, buttonWidth);
        addArmorToggle("Chestplate", ArmorVisibilityConfig.ArmorPart.CHESTPLATE, rightX, startY, buttonWidth);
        addArmorToggle("Leggings", ArmorVisibilityConfig.ArmorPart.LEGGINGS, leftX, startY + 28, buttonWidth);
        addArmorToggle("Boots", ArmorVisibilityConfig.ArmorPart.BOOTS, rightX, startY + 28, buttonWidth);
        addRenderableWidget(createToggleButton("Global Armor", ArmorVisibilityConfig.INSTANCE::isArmorVisible,
                ArmorVisibilityConfig.INSTANCE::setArmorVisible,
                leftX + (contentWidth - buttonWidth) / 2, startY + 56, buttonWidth));
        addRenderableWidget(createToggleButton("Cape", ArmorVisibilityConfig.INSTANCE::shouldKeepCapeVisible,
                ArmorVisibilityConfig.INSTANCE::setKeepCapeVisible, leftX, startY + 92, buttonWidth));
        addRenderableWidget(createToggleButton("Elytra", ArmorVisibilityConfig.INSTANCE::shouldKeepElytraVisible,
                ArmorVisibilityConfig.INSTANCE::setKeepElytraVisible, rightX, startY + 92, buttonWidth));
        addRenderableWidget(createToggleButton("Player Only", ArmorVisibilityConfig.INSTANCE::isPlayerOnly,
                ArmorVisibilityConfig.INSTANCE::setPlayerOnly, leftX, startY + 120, buttonWidth));
        addRenderableWidget(createToggleButton("Other Armor", ArmorVisibilityConfig.INSTANCE::shouldHideForOtherPlayers,
                ArmorVisibilityConfig.INSTANCE::setHideForOtherPlayers, rightX, startY + 120, buttonWidth));

        int doneWidth = Math.min(200, Math.max(0, width - 32));
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> Minecraft.getInstance().setScreen(parent))
                .bounds((width - doneWidth) / 2, height - 32, doneWidth, 20).build());
    }

    private void addArmorToggle(String label, ArmorVisibilityConfig.ArmorPart part, int x, int y, int buttonWidth) {
        addRenderableWidget(Button.builder(armorPartText(label, part), button -> {
            ArmorVisibilityConfig.INSTANCE.setArmorPartHidden(part.getSlot(),
                    !ArmorVisibilityConfig.INSTANCE.isArmorPartHidden(part.getSlot()));
            rebuildWidgets();
        }).bounds(x, y, buttonWidth, 20).build());
    }

    private Button createToggleButton(String label, java.util.function.BooleanSupplier state,
                                      java.util.function.Consumer<Boolean> setter, int x, int y, int buttonWidth) {
        return Button.builder(toggleText(label, state.getAsBoolean()), button -> {
            setter.accept(!state.getAsBoolean());
            rebuildWidgets();
        }).bounds(x, y, buttonWidth, 20).build();
    }

    private Component armorPartText(String label, ArmorVisibilityConfig.ArmorPart part) {
        boolean hidden = ArmorVisibilityConfig.INSTANCE.isArmorPartHidden(part.getSlot());
        return Component.literal(label + ": " + (hidden ? "Hidden" : "Shown"));
    }

    private Component toggleText(String label, boolean enabled) {
        if (label.equals("Other Armor")) {
            return Component.literal(label + ": " + (enabled ? "Hidden" : "Visible"));
        }
        return Component.literal(label + ": " + (enabled ? "On" : "Off"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(font, title, width / 2, 20, 0xFFFFFF);
    }
}
