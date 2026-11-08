package com.example.armorvisibility.compat;

import com.example.armorvisibility.ArmorConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;

import java.time.Duration;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ConfigScreen extends Screen {
    private final Screen parent;

    private int settingsX;
    private int settingsWidth;
    private int contentWidth;
    private int previewX;
    private int previewWidth;
    private int startY;
    private float layoutScale = 1.0F;
    private int controlHeight = 20;
    private int presetHeight = 30;
    private int presetY;
    private boolean showPreview;

    public ConfigScreen(Screen parent) {
        super(translated("armorvisibility.screen.title"));
        this.parent = parent;
        ArmorConfig.INSTANCE.load();
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
        layoutScale = Math.min(1.0F, Math.max(0.1F,
                (height - 28.0F - 6.0F - startY - 30.0F) / 245.0F));
        controlHeight = Math.max(8, Math.round(20 * layoutScale));
        presetHeight = Math.max(26, Math.round(30 * layoutScale));

        int toggleGap = 4;
        int toggleWidth = (settingsWidth - toggleGap) / 2;
        addToggleButton("armorvisibility.setting.cape", ArmorConfig.INSTANCE::shouldKeepCapeVisible,
                ArmorConfig.INSTANCE::setKeepCapeVisible, settingsX, startY, toggleWidth);
        addToggleButton("armorvisibility.setting.elytra", ArmorConfig.INSTANCE::shouldKeepElytraVisible,
                ArmorConfig.INSTANCE::setKeepElytraVisible,
                settingsX + toggleWidth + toggleGap, startY, toggleWidth);

        addToggleButton("armorvisibility.setting.global_armor", ArmorConfig.INSTANCE::isArmorVisible,
                ArmorConfig.INSTANCE::setArmorVisible, settingsX, layoutY(24), toggleWidth);
        addToggleButton("armorvisibility.setting.player_only", ArmorConfig.INSTANCE::isPlayerOnly,
                ArmorConfig.INSTANCE::setPlayerOnly,
                settingsX + toggleWidth + toggleGap, layoutY(24), toggleWidth);

        addArmorControls("armorvisibility.setting.helmet", ArmorConfig.ArmorPart.HELMET, layoutY(48));
        addArmorControls("armorvisibility.setting.chestplate", ArmorConfig.ArmorPart.CHESTPLATE, layoutY(74));
        addArmorControls("armorvisibility.setting.leggings", ArmorConfig.ArmorPart.LEGGINGS, layoutY(100));
        addArmorControls("armorvisibility.setting.boots", ArmorConfig.ArmorPart.BOOTS, layoutY(126));

        addOpacitySlider("armorvisibility.setting.offhand", ArmorConfig.INSTANCE.getOffhandOpacity(),
                ArmorConfig.INSTANCE::setOffhandOpacity, settingsWidth, layoutY(152));
        addToggleButton("armorvisibility.setting.apply_to_others", ArmorConfig.INSTANCE::shouldHideForOtherPlayers,
                ArmorConfig.INSTANCE::setHideForOtherPlayers, settingsX, layoutY(178), settingsWidth);

        presetY = layoutY(245);
        addPresets();
        int doneWidth = Math.min(200, Math.max(0, width - 32));
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds((width - doneWidth) / 2, height - 28, doneWidth, 20).build());
    }

    private String localizePresetDescription(String details) {
        return details
                .replace("Empty preset", translated("armorvisibility.preset.empty").getString())
                .replace("Click to save current settings", translated("armorvisibility.preset.save_hint").getString())
                .replace("Left-click to save current settings", translated("armorvisibility.preset.save_hint").getString())
                .replace("Armor", translated("armorvisibility.preset.armor").getString())
                .replace("Helmet", translated("armorvisibility.preset.helmet").getString())
                .replace("Chestplate", translated("armorvisibility.preset.chestplate").getString())
                .replace("Leggings", translated("armorvisibility.preset.leggings").getString())
                .replace("Boots", translated("armorvisibility.preset.boots").getString())
                .replace("Glint", translated("armorvisibility.preset.glint").getString())
                .replace("Offhand", translated("armorvisibility.preset.offhand").getString())
                .replace("Cape", translated("armorvisibility.preset.cape").getString())
                .replace("Elytra", translated("armorvisibility.preset.elytra").getString())
                .replace("Player only", translated("armorvisibility.preset.player_only").getString())
                .replace("Others", translated("armorvisibility.preset.others").getString())
                .replace("Left-click to load", translated("armorvisibility.preset.load_hint").getString())
                .replace("Right-click to delete", translated("armorvisibility.preset.delete_hint").getString())
                .replace("ON", translated("armorvisibility.state.on").getString())
                .replace("OFF", translated("armorvisibility.state.off").getString());
    }
    private static Component translated(String key, Object... arguments) {
        String fallback = switch (key) {
            case "armorvisibility.screen.title" -> "Armor Visibility";
            case "armorvisibility.setting.cape" -> "Cape";
            case "armorvisibility.setting.elytra" -> "Elytra";
            case "armorvisibility.setting.global_armor" -> "Global Armor";
            case "armorvisibility.setting.player_only" -> "Player Only";
            case "armorvisibility.setting.helmet" -> "Helmet";
            case "armorvisibility.setting.chestplate" -> "Chestplate";
            case "armorvisibility.setting.leggings" -> "Leggings";
            case "armorvisibility.setting.boots" -> "Boots";
            case "armorvisibility.setting.offhand" -> "Offhand";
            case "armorvisibility.setting.apply_to_others" -> "Apply to Others";
            case "armorvisibility.setting.toggle" -> "%s: %s";
            case "armorvisibility.setting.opacity" -> "%s: %s%%";
            case "armorvisibility.setting.glint" -> "Glint: %s";
            case "armorvisibility.state.on" -> "ON";
            case "armorvisibility.state.off" -> "OFF";
            case "armorvisibility.presets.title" -> "Presets";
            case "armorvisibility.presets.instructions" -> "Left-click: save/load | Right-click: delete";
            case "armorvisibility.preview.title" -> "Preview";
            case "armorvisibility.preset.title" -> "PRESET %s";
            case "armorvisibility.preset.short" -> "P%s";
            case "armorvisibility.preset.saved" -> "SAVED";
            case "armorvisibility.preset.save" -> "+ SAVE";
            case "armorvisibility.preset.empty" -> "Empty preset";
            case "armorvisibility.preset.save_hint" -> "Left-click to save current settings";
            case "armorvisibility.preset.armor" -> "Armor";
            case "armorvisibility.preset.helmet" -> "Helmet";
            case "armorvisibility.preset.chestplate" -> "Chestplate";
            case "armorvisibility.preset.leggings" -> "Leggings";
            case "armorvisibility.preset.boots" -> "Boots";
            case "armorvisibility.preset.glint" -> "Glint";
            case "armorvisibility.preset.offhand" -> "Offhand";
            case "armorvisibility.preset.cape" -> "Cape";
            case "armorvisibility.preset.elytra" -> "Elytra";
            case "armorvisibility.preset.player_only" -> "Player only";
            case "armorvisibility.preset.others" -> "Others";
            case "armorvisibility.preset.delete_hint" -> "Right-click to delete";
            case "armorvisibility.preset.load_hint" -> "Left-click to load";
            default -> key;
        };
        return Component.translatableWithFallback(key, fallback, arguments);
    }

    private int layoutY(int offset) {
        return startY + Math.round(offset * layoutScale);
    }

    private void addArmorControls(String label, ArmorConfig.ArmorPart part, int y) {
        int glintWidth = Math.min(58, settingsWidth / 3);
        int gap = 4;
        int sliderWidth = Math.max(0, settingsWidth - glintWidth - gap);
        addOpacitySlider(label, ArmorConfig.INSTANCE.getArmorOpacity(part.getSlot()),
                opacity -> ArmorConfig.INSTANCE.setArmorOpacity(part.getSlot(), opacity),
                sliderWidth, y);

        addRenderableWidget(Button.builder(glintText(part.getSlot()), button -> {
            EquipmentSlot slot = part.getSlot();
            ArmorConfig.INSTANCE.setShowGlint(slot,
                    !ArmorConfig.INSTANCE.shouldShowGlint(slot));
            rebuildWidgets();
        }).bounds(settingsX + sliderWidth + gap, y, glintWidth, controlHeight).build());
    }

    private void addOpacitySlider(String label, float opacity, Consumer<Float> setter, int sliderWidth, int y) {
        addRenderableWidget(new OpacitySlider(settingsX, y, sliderWidth, controlHeight, label, opacity, setter));
    }

    private void addToggleButton(String label, BooleanSupplier state, Consumer<Boolean> setter,
                                 int x, int y, int buttonWidth) {
        addRenderableWidget(Button.builder(toggleText(label, state.getAsBoolean()), button -> {
            setter.accept(!state.getAsBoolean());
            rebuildWidgets();
        }).bounds(x, y, buttonWidth, controlHeight).build());
    }

    private Component toggleText(String label, boolean enabled) {
        return translated("armorvisibility.setting.toggle", translated(label), translated(enabled ? "armorvisibility.state.on" : "armorvisibility.state.off"));
    }

    private Component glintText(EquipmentSlot slot) {
        return translated("armorvisibility.setting.glint", translated(ArmorConfig.INSTANCE.shouldShowGlint(slot) ? "armorvisibility.state.on" : "armorvisibility.state.off"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(font, title, width / 2, 18, 0xFFFFFF);
        graphics.drawString(font, translated("armorvisibility.presets.title"), settingsX, layoutY(204), 0xFFFFFF);
        Component instructions = translated("armorvisibility.presets.instructions");
        graphics.drawString(font, instructions, settingsX + contentWidth - font.width(instructions),
                layoutY(204), 0xFFB8B8B8);

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
        graphics.drawCenteredString(font, translated("armorvisibility.preview.title"),
                previewX + previewWidth / 2, panelY + 8, 0xFFFFFF);

        int entitySize = Math.round(Math.min(previewWidth, panelBottom - panelY) * 0.42F);
        InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, previewX + 1, panelY + 1,
                panelRight - 1, panelBottom - 1, entitySize, 0.0F, mouseX, mouseY, minecraft.player);
    }

    private void addPresets() {
        int gap = 3;
        int cardWidth = Math.max(1, (contentWidth - gap * 4) / 5);
        for (int index = 0; index < 5; index++) {
            int x = settingsX + index * (cardWidth + gap);
            addRenderableWidget(new PresetButton(index, x, presetY, cardWidth, presetHeight));
        }
    }

    private final class PresetButton extends AbstractWidget {
        private final int index;

        private PresetButton(int index, int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty());
            this.index = index;
            setTooltipDelay(java.time.Duration.ofMillis(250));
            updateDisplay();
        }

        private void updateDisplay() {
            boolean populated = ArmorConfig.INSTANCE.hasPreset(index);
            Component presetTitle = translated("armorvisibility.preset.title", index + 1);
            setMessage(getWidth() < font.width(presetTitle) + 8
                    ? translated("armorvisibility.preset.short", index + 1)
                    : presetTitle);
            String details = ArmorConfig.INSTANCE.describePreset(index);
            if (populated) {
                details = details.replace("\nRight-click to delete",
                        "\nLeft-click to load\nRight-click to delete");
            }
            setTooltip(Tooltip.create(Component.literal(localizePresetDescription(details))));
        }

        private void handleClick(int button) {
            boolean populated = ArmorConfig.INSTANCE.hasPreset(index);
            if (button == 1) {
                if (populated) {
                    ArmorConfig.INSTANCE.deletePreset(index);
                }
                updateDisplay();
            } else if (button == 0) {
                if (populated) {
                    ArmorConfig.INSTANCE.applyPreset(index);
                    rebuildWidgets();
                } else {
                    ArmorConfig.INSTANCE.savePreset(index);
                    updateDisplay();
                }
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (active && visible && isMouseOver(mouseX, mouseY) && button == 1) {
                handleClick(button);
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            handleClick(0);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            boolean populated = ArmorConfig.INSTANCE.hasPreset(index);
            int background = !active ? 0xFF3F3F3F : isHovered() ? 0xFF707070 : 0xFF555555;
            int highlight = !active ? 0xFF686868 : 0xFFAAAAAA;
            int shadow = !active ? 0xFF202020 : 0xFF373737;

            graphics.fill(getX(), getY(), getRight(), getBottom(), 0xFF000000);
            graphics.fill(getX() + 1, getY() + 1, getRight() - 1, getBottom() - 1, background);
            graphics.fill(getX() + 1, getY() + 1, getRight() - 1, getY() + 2, highlight);
            graphics.fill(getX() + 1, getY() + 1, getX() + 2, getBottom() - 1, highlight);
            graphics.fill(getX() + 1, getBottom() - 2, getRight() - 1, getBottom() - 1, shadow);
            graphics.fill(getRight() - 2, getY() + 1, getRight() - 1, getBottom() - 1, shadow);

            graphics.drawCenteredString(font, getMessage(), getX() + getWidth() / 2,
                    getY() + (getHeight() - 18) / 2, 0xFFFFFFFF);
            graphics.drawCenteredString(font, (getWidth() < 54 ? Component.empty() : translated(
                    populated ? "armorvisibility.preset.saved" : "armorvisibility.preset.save")),
                    getX() + getWidth() / 2, getY() + (getHeight() - 18) / 2 + 11, 0xFFDDDDDD);
}

        @Override
        public void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }

    @Override
    public void removed() {
        ArmorConfig.INSTANCE.save();
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
            super(x, y, width, height, translated(label), opacity);
            this.label = label;
            this.setter = setter;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(translated("armorvisibility.setting.opacity", translated(label), Math.round(value * 100.0)));
        }

        @Override
        protected void applyValue() {
            setter.accept((float) value);
        }
    }
}
