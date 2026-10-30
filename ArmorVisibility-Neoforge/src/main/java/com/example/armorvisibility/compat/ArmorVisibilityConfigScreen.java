package com.example.armorvisibility.compat;

import com.example.armorvisibility.ArmorVisibilityConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ArmorVisibilityConfigScreen extends Screen {
    private final Screen parent;

    private int settingsX;
    private int settingsWidth;
    private int contentWidth;
    private int previewX;
    private int previewWidth;
    private int startY;
    private int presetY;
    private boolean showPreview;

    public ArmorVisibilityConfigScreen(Screen parent) {
        super(Component.literal("Armor Visibility"));
        this.parent = parent;
        ArmorVisibilityConfig.INSTANCE.load();
    }

    @Override
    protected void init() {
        super.init();

        contentWidth = Math.min(368, Math.max(0, width - 32));
        showPreview = contentWidth >= 280;
        settingsWidth = showPreview ? (contentWidth - 12) * 60 / 100 : contentWidth;
        settingsX = (width - contentWidth) / 2;
        previewX = settingsX + settingsWidth + 12;
        previewWidth = showPreview ? contentWidth - settingsWidth - 12 : 0;
        startY = Math.max(36, (height - 350) / 2);

        int toggleGap = 4;
        int toggleWidth = (settingsWidth - toggleGap) / 2;
        addToggleButton("Cape", ArmorVisibilityConfig.INSTANCE::shouldKeepCapeVisible,
                ArmorVisibilityConfig.INSTANCE::setKeepCapeVisible, settingsX, startY, toggleWidth);
        addToggleButton("Elytra", ArmorVisibilityConfig.INSTANCE::shouldKeepElytraVisible,
                ArmorVisibilityConfig.INSTANCE::setKeepElytraVisible,
                settingsX + toggleWidth + toggleGap, startY, toggleWidth);

        addToggleButton("Global Armor", ArmorVisibilityConfig.INSTANCE::isArmorVisible,
                ArmorVisibilityConfig.INSTANCE::setArmorVisible, settingsX, startY + 24, toggleWidth);
        addToggleButton("Player Only", ArmorVisibilityConfig.INSTANCE::isPlayerOnly,
                ArmorVisibilityConfig.INSTANCE::setPlayerOnly,
                settingsX + toggleWidth + toggleGap, startY + 24, toggleWidth);

        addArmorControls("Helmet", ArmorVisibilityConfig.ArmorPart.HELMET, startY + 48);
        addArmorControls("Chestplate", ArmorVisibilityConfig.ArmorPart.CHESTPLATE, startY + 74);
        addArmorControls("Leggings", ArmorVisibilityConfig.ArmorPart.LEGGINGS, startY + 100);
        addArmorControls("Boots", ArmorVisibilityConfig.ArmorPart.BOOTS, startY + 126);

        addOpacitySlider("Offhand", ArmorVisibilityConfig.INSTANCE.getOffhandOpacity(),
                ArmorVisibilityConfig.INSTANCE::setOffhandOpacity, settingsWidth, startY + 152);
        addToggleButton("Apply to Others", ArmorVisibilityConfig.INSTANCE::shouldHideForOtherPlayers,
                ArmorVisibilityConfig.INSTANCE::setHideForOtherPlayers, settingsX, startY + 178, settingsWidth);

        presetY = startY + 245;
        int doneWidth = Math.min(200, Math.max(0, width - 32));
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds((width - doneWidth) / 2, height - 28, doneWidth, 20).build());
    }

    private void addArmorControls(String label, ArmorVisibilityConfig.ArmorPart part, int y) {
        int glintWidth = Math.min(58, settingsWidth / 3);
        int gap = 4;
        int sliderWidth = Math.max(0, settingsWidth - glintWidth - gap);
        addOpacitySlider(label, ArmorVisibilityConfig.INSTANCE.getArmorOpacity(part.getSlot()),
                opacity -> ArmorVisibilityConfig.INSTANCE.setArmorOpacity(part.getSlot(), opacity),
                sliderWidth, y);

        addRenderableWidget(Button.builder(glintText(part.getSlot()), button -> {
            EquipmentSlot slot = part.getSlot();
            ArmorVisibilityConfig.INSTANCE.setShowGlint(slot,
                    !ArmorVisibilityConfig.INSTANCE.shouldShowGlint(slot));
            rebuildWidgets();
        }).bounds(settingsX + sliderWidth + gap, y, glintWidth, 20).build());
    }

    private void addOpacitySlider(String label, float opacity, Consumer<Float> setter, int sliderWidth, int y) {
        addRenderableWidget(new OpacitySlider(settingsX, y, sliderWidth, 20, label, opacity, setter));
    }

    private void addToggleButton(String label, BooleanSupplier state, Consumer<Boolean> setter,
                                 int x, int y, int buttonWidth) {
        addRenderableWidget(Button.builder(toggleText(label, state.getAsBoolean()), button -> {
            setter.accept(!state.getAsBoolean());
            rebuildWidgets();
        }).bounds(x, y, buttonWidth, 20).build());
    }

    private Component toggleText(String label, boolean enabled) {
        return Component.literal(label + ": " + (enabled ? "ON" : "OFF"));
    }

    private Component glintText(EquipmentSlot slot) {
        return Component.literal("Glint: "
                + (ArmorVisibilityConfig.INSTANCE.shouldShowGlint(slot) ? "ON" : "OFF"));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int gap = 3;
        int cardWidth = Math.max(1, (contentWidth - gap * 4) / 5);
        if (event.y() >= presetY && event.y() < presetY + 30) {
            for (int index = 0; index < 5; index++) {
                int left = settingsX + index * (cardWidth + gap);
                if (event.x() < left || event.x() >= left + cardWidth) {
                    continue;
                }

                if (event.button() == 1) {
                    if (ArmorVisibilityConfig.INSTANCE.hasPreset(index)) {
                        ArmorVisibilityConfig.INSTANCE.deletePreset(index);
                    }
                    return true;
                }
                if (event.button() == 0) {
                    if (ArmorVisibilityConfig.INSTANCE.hasPreset(index)) {
                        ArmorVisibilityConfig.INSTANCE.applyPreset(index);
                    } else {
                        ArmorVisibilityConfig.INSTANCE.savePreset(index);
                    }
                    rebuildWidgets();
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(font, title, width / 2, 18, 0xFFFFFF);
        Component instructions = Component.literal("Left-click: save/load | Right-click: delete");
        graphics.drawString(font, instructions, settingsX + contentWidth - font.width(instructions),
                startY + 204, 0xFFB8B8B8);
        drawPresetCards(graphics, mouseX, mouseY);

        Minecraft minecraft = Minecraft.getInstance();
        if (!showPreview || minecraft.player == null || previewWidth < 80) {
            return;
        }

        int panelY = startY;
        int panelBottom = Math.min(height - 38, panelY + Math.min(220, previewWidth + 24));
        if (panelBottom - panelY < 70) {
            return;
        }

        int panelRight = previewX + previewWidth;
        graphics.fill(previewX, panelY, panelRight, panelBottom, 0xCC202020);
        graphics.fill(previewX, panelY, panelRight, panelY + 1, 0xFFC0C0C0);
        graphics.fill(previewX, panelBottom - 1, panelRight, panelBottom, 0xFFC0C0C0);
        graphics.fill(previewX, panelY, previewX + 1, panelBottom, 0xFFC0C0C0);
        graphics.fill(panelRight - 1, panelY, panelRight, panelBottom, 0xFFC0C0C0);
        graphics.drawCenteredString(font, Component.literal("Preview"),
                previewX + previewWidth / 2, panelY + 8, 0xFFFFFF);

        int entitySize = Math.round(Math.min(previewWidth, panelBottom - panelY) * 0.42F);
        InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, previewX + 1, panelY + 1,
                panelRight - 1, panelBottom - 1, entitySize, 0.0F, mouseX, mouseY, minecraft.player);
    }

    private void drawPresetCards(GuiGraphics graphics, int mouseX, int mouseY) {
        int gap = 3;
        int cardWidth = Math.max(1, (contentWidth - gap * 4) / 5);
        for (int index = 0; index < 5; index++) {
            int x = settingsX + index * (cardWidth + gap);
            int right = x + cardWidth;
            boolean populated = ArmorVisibilityConfig.INSTANCE.hasPreset(index);
            boolean hovered = mouseX >= x && mouseX < right && mouseY >= presetY && mouseY < presetY + 30;

            int background = hovered ? (populated ? 0xFF40589A : 0xFF383D4C)
                    : (populated ? 0xFF303D62 : 0xFF292D3A);
            graphics.fill(x, presetY, right, presetY + 30, 0xFF111111);
            graphics.fill(x + 1, presetY + 1, right - 1, presetY + 29, background);
            graphics.fill(x + 1, presetY + 1, right - 1, presetY + 3,
                    populated ? 0xFF7FA4FF : 0xFF626A7C);
            graphics.drawCenteredString(font, Component.literal("PRESET " + (index + 1)),
                    x + cardWidth / 2, presetY + 6, 0xFFFFFFFF);
            graphics.drawCenteredString(font, Component.literal(populated ? "SAVED" : "+ SAVE"),
                    x + cardWidth / 2, presetY + 17, populated ? 0xFFB9CBFF : 0xFFBFC3CC);

            if (hovered) {
                List<FormattedCharSequence> tooltip = font.split(
                        Component.literal(ArmorVisibilityConfig.INSTANCE.describePreset(index)), 220);
                graphics.setTooltipForNextFrame(font, tooltip, mouseX, mouseY);
            }
        }
    }

    @Override
    public void removed() {
        ArmorVisibilityConfig.INSTANCE.save();
        super.removed();
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    private static final class OpacitySlider extends AbstractSliderButton {
        private final String label;
        private final Consumer<Float> setter;

        private OpacitySlider(int x, int y, int width, int height, String label,
                              float opacity, Consumer<Float> setter) {
            super(x, y, width, height, Component.literal(label), opacity);
            this.label = label;
            this.setter = setter;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(label + ": " + Math.round(value * 100.0) + "%"));
        }

        @Override
        protected void applyValue() {
            setter.accept((float) value);
        }
    }
}
