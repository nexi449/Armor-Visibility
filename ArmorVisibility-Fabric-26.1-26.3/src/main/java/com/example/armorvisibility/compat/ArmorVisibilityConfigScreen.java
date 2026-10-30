package com.example.armorvisibility.compat;

import com.example.armorvisibility.ArmorVisibilityConfig;
import com.example.armorvisibility.ArmorVisibilityClient;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;

import java.time.Duration;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
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
        addPresets();

        int doneWidth = Math.min(200, Math.max(0, width - 32));
        addRenderableWidget(Button.builder(Component.translatable("gui.done"),
                        button -> ArmorVisibilityClient.showScreen(Minecraft.getInstance(), parent))
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

    private void addPresets() {
        int gap = 3;
        int cardWidth = Math.max(1, (contentWidth - gap * 4) / 5);
        for (int index = 0; index < 5; index++) {
            int x = settingsX + index * (cardWidth + gap);
            addRenderableWidget(new PresetButton(index, x, presetY, cardWidth, 30));
        }
    }

    private void handlePresetClick(int index, int button) {
        boolean populated = ArmorVisibilityConfig.INSTANCE.hasPreset(index);
        if (button == InputConstants.MOUSE_BUTTON_RIGHT && populated) {
            ArmorVisibilityConfig.INSTANCE.deletePreset(index);
        } else if (button == InputConstants.MOUSE_BUTTON_LEFT && populated) {
            ArmorVisibilityConfig.INSTANCE.applyPreset(index);
        } else if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            ArmorVisibilityConfig.INSTANCE.savePreset(index);
        }
        rebuildWidgets();
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
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        context.centeredText(font, title, width / 2, 18, 0xFFFFFF);
        context.text(font, Component.literal("Presets"), settingsX, startY + 204, 0xFFFFFF);
        Component instructions = Component.literal("Left-click: save/load | Right-click: delete");
        context.text(font, instructions, settingsX + contentWidth - font.width(instructions),
                startY + 204, 0xFFB8B8B8);

        if (!showPreview || Minecraft.getInstance().player == null || previewWidth < 80) {
            return;
        }

        int panelY = startY;
        int panelBottom = Math.min(height - 38, panelY + Math.min(220, previewWidth + 24));
        if (panelBottom - panelY < 70) {
            return;
        }

        int panelRight = previewX + previewWidth;
        context.fill(previewX, panelY, panelRight, panelBottom, 0xCC202020);
        context.fill(previewX, panelY, panelRight, panelY + 1, 0xFFC0C0C0);
        context.fill(previewX, panelBottom - 1, panelRight, panelBottom, 0xFFC0C0C0);
        context.fill(previewX, panelY, previewX + 1, panelBottom, 0xFFC0C0C0);
        context.fill(panelRight - 1, panelY, panelRight, panelBottom, 0xFFC0C0C0);
        context.centeredText(font, Component.literal("Preview"), previewX + previewWidth / 2, panelY + 8,
                0xFFFFFF);
        InventoryScreen.extractEntityInInventoryFollowsMouse(context, previewX + 1, panelY + 1,
                panelRight - 1, panelBottom - 1, Math.round(Math.min(previewWidth,
                        panelBottom - panelY) * 0.42F), 0.0F, mouseX, mouseY, Minecraft.getInstance().player);
    }

    @Override
    public void removed() {
        ArmorVisibilityConfig.INSTANCE.save();
        super.removed();
    }

    private final class PresetButton extends AbstractWidget {
        private final int index;

        private PresetButton(int index, int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty());
            this.index = index;
            setTooltipDelay(Duration.ofMillis(250));
            updateDisplay();
        }

        private void updateDisplay() {
            boolean populated = ArmorVisibilityConfig.INSTANCE.hasPreset(index);
            setMessage(Component.literal("PRESET " + (index + 1)));
            String details = ArmorVisibilityConfig.INSTANCE.describePreset(index);
            if (populated) {
                details = details.replace("\nRight-click to delete",
                        "\nLeft-click to load\nRight-click to delete");
            } else {
                details = details.replace("Left-click to save current settings",
                        "Click to save current settings");
            }
            setTooltip(Tooltip.create(Component.literal(details)));
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean doubleClick) {
            handlePresetClick(index, event.button());
        }

        @Override
        protected boolean isValidClickButton(MouseButtonInfo button) {
            return button.button() == InputConstants.MOUSE_BUTTON_LEFT
                    || button.button() == InputConstants.MOUSE_BUTTON_RIGHT;
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY,
                                                float delta) {
            boolean populated = ArmorVisibilityConfig.INSTANCE.hasPreset(index);
            int background = !active ? 0xFF242424
                    : isHovered() ? (populated ? 0xFF40589A : 0xFF383D4C)
                    : (populated ? 0xFF303D62 : 0xFF292D3A);
            context.fill(getX(), getY(), getRight(), getBottom(), 0xFF111111);
            context.fill(getX() + 1, getY() + 1, getRight() - 1, getBottom() - 1, background);
            context.fill(getX() + 1, getY() + 1, getRight() - 1, getY() + 3,
                    populated ? 0xFF7FA4FF : 0xFF626A7C);
            context.centeredText(font, getMessage(), getX() + getWidth() / 2, getY() + 6, 0xFFFFFFFF);
            Component status = Component.literal(populated ? "SAVED" : "+ SAVE");
            context.centeredText(font, status, getX() + getWidth() / 2, getY() + 17,
                    populated ? 0xFFB9CBFF : 0xFFBFC3CC);
            handleCursor(context);
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
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
