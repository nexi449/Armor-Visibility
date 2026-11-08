package com.example.armorvisibility.compat;

import com.example.armorvisibility.ArmorConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.input.MouseInput;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.time.Duration;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
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
    private int presetHeadingY;
    private boolean showPreview;

    public ConfigScreen(Screen parent) {
        super(Text.translatable("armorvisibility.screen.title"));
        this.parent = parent;
        ArmorConfig.INSTANCE.load();
    }

    @Override
    protected void init() {
        super.init();

        contentWidth = Math.min(368, Math.max(0, width - 32));
        showPreview = contentWidth >= 280;
        settingsWidth = showPreview ? (contentWidth - 12) * 60 / 100 : contentWidth;
        int contentX = (width - contentWidth) / 2;
        settingsX = contentX;
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

        presetHeadingY = layoutY(204);
        addPresets(layoutY(245));

        int doneWidth = Math.min(200, Math.max(0, width - 32));
        addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> close())
                .dimensions((width - doneWidth) / 2, height - 28, doneWidth, 20)
                .build());
    }

    private void addPresets(int y) {
        int gap = 3;
        int presetWidth = Math.max(1, (contentWidth - gap * 4) / 5);
        for (int index = 0; index < 5; index++) {
            int x = settingsX + index * (presetWidth + gap);
            addDrawableChild(new PresetButton(index, x, y, presetWidth, presetHeight));
        }
    }

    private String localizePresetDescription(String details) {
        return details
                .replace("Empty preset", Text.translatable("armorvisibility.preset.empty").getString())
                .replace("Click to save current settings", Text.translatable("armorvisibility.preset.save_hint").getString())
                .replace("Left-click to save current settings", Text.translatable("armorvisibility.preset.save_hint").getString())
                .replace("Armor", Text.translatable("armorvisibility.preset.armor").getString())
                .replace("Helmet", Text.translatable("armorvisibility.preset.helmet").getString())
                .replace("Chestplate", Text.translatable("armorvisibility.preset.chestplate").getString())
                .replace("Leggings", Text.translatable("armorvisibility.preset.leggings").getString())
                .replace("Boots", Text.translatable("armorvisibility.preset.boots").getString())
                .replace("Glint", Text.translatable("armorvisibility.preset.glint").getString())
                .replace("Offhand", Text.translatable("armorvisibility.preset.offhand").getString())
                .replace("Cape", Text.translatable("armorvisibility.preset.cape").getString())
                .replace("Elytra", Text.translatable("armorvisibility.preset.elytra").getString())
                .replace("Player only", Text.translatable("armorvisibility.preset.player_only").getString())
                .replace("Others", Text.translatable("armorvisibility.preset.others").getString())
                .replace("Left-click to load", Text.translatable("armorvisibility.preset.load_hint").getString())
                .replace("Right-click to delete", Text.translatable("armorvisibility.preset.delete_hint").getString())
                .replace("ON", Text.translatable("armorvisibility.state.on").getString())
                .replace("OFF", Text.translatable("armorvisibility.state.off").getString());
    }
    private int layoutY(int offset) {
        return startY + Math.round(offset * layoutScale);
    }

    private void addArmorControls(String label, ArmorConfig.ArmorPart part, int y) {
        int glintWidth = Math.min(58, settingsWidth / 3);
        int gap = 4;
        int sliderWidth = Math.max(0, settingsWidth - glintWidth - gap);
        addOpacitySlider(
                label,
                ArmorConfig.INSTANCE.getArmorOpacity(part.getSlot()),
                opacity -> ArmorConfig.INSTANCE.setArmorOpacity(part.getSlot(), opacity),
                sliderWidth,
                y
        );

        addDrawableChild(ButtonWidget.builder(glintText(part.getSlot()), button -> {
            EquipmentSlot slot = part.getSlot();
            ArmorConfig.INSTANCE.setShowGlint(slot, !ArmorConfig.INSTANCE.shouldShowGlint(slot));
            button.setMessage(glintText(slot));
        }).dimensions(settingsX + sliderWidth + gap, y, glintWidth, controlHeight).build());
    }

    private void addOpacitySlider(String label, float opacity, Consumer<Float> setter, int sliderWidth, int y) {
        addDrawableChild(new OpacitySlider(
                settingsX,
                y,
                sliderWidth,
                controlHeight,
                label,
                opacity,
                setter
        ));
    }

    private void addToggleButton(String label, BooleanSupplier state, Consumer<Boolean> setter,
                                 int x, int y, int buttonWidth) {
        addDrawableChild(ButtonWidget.builder(toggleText(label, state.getAsBoolean()), button -> {
            setter.accept(!state.getAsBoolean());
            button.setMessage(toggleText(label, state.getAsBoolean()));
        }).dimensions(x, y, buttonWidth, controlHeight).build());
    }

    private Text toggleText(String label, boolean enabled) {
        return Text.translatable("armorvisibility.setting.toggle", Text.translatable(label), Text.translatable(enabled ? "armorvisibility.state.on" : "armorvisibility.state.off"));
    }

    private Text glintText(EquipmentSlot slot) {
        return Text.translatable("armorvisibility.setting.glint", Text.translatable(ArmorConfig.INSTANCE.shouldShowGlint(slot) ? "armorvisibility.state.on" : "armorvisibility.state.off"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 18, 0xFFFFFF);
        context.drawTextWithShadow(textRenderer, Text.translatable("armorvisibility.presets.title"), settingsX, presetHeadingY, 0xFFFFFF);
        Text presetInstructions = Text.translatable("armorvisibility.presets.instructions");
        context.drawTextWithShadow(textRenderer, presetInstructions,
                settingsX + contentWidth - textRenderer.getWidth(presetInstructions),
                presetHeadingY, 0xFFB8B8B8);
        if (!showPreview || client == null || client.player == null || previewWidth < 80) {
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
        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("armorvisibility.preview.title"),
                previewX + previewWidth / 2, panelY + 8, 0xFFFFFF);

        int entitySize = Math.round(Math.min(previewWidth, panelBottom - panelY) * 0.42F);
        InventoryScreen.drawEntity(context, previewX + 1, panelY + 1, panelRight - 1, panelBottom - 1,
                entitySize, 0.0F, mouseX, mouseY, client.player);
    }

    @Override
    public void removed() {
        ArmorConfig.INSTANCE.save();
        super.removed();
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }

    private static final class OpacitySlider extends SliderWidget {
        private final String label;
        private final Consumer<Float> setter;

        private OpacitySlider(int x, int y, int width, int height, String label,
                              float opacity, Consumer<Float> setter) {
            super(x, y, width, height, Text.translatable(label), opacity);
            this.label = label;
            this.setter = setter;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Text.translatable("armorvisibility.setting.opacity", Text.translatable(label), Math.round(value * 100.0)));
        }

        @Override
        protected void applyValue() {
            setter.accept((float) value);
        }
    }

    private final class PresetButton extends ClickableWidget {
        private final int index;

        private PresetButton(int index, int x, int y, int width, int height) {
            super(x, y, width, height, Text.empty());
            this.index = index;
            setTooltipDelay(Duration.ofMillis(250));
            updateDisplay();
        }

        private void updateDisplay() {
            boolean populated = ArmorConfig.INSTANCE.hasPreset(index);
            Text presetTitle = Text.translatable("armorvisibility.preset.title", index + 1);
            setMessage(getWidth() < textRenderer.getWidth(presetTitle) + 8
                    ? Text.translatable("armorvisibility.preset.short", index + 1)
                    : presetTitle);
            String details = ArmorConfig.INSTANCE.describePreset(index);
            if (populated) {
                details = details.replace("\nRight-click to delete", "\nLeft-click to load\nRight-click to delete");
            }
            setTooltip(Tooltip.of(Text.literal(localizePresetDescription(details))));
        }

        @Override
        public void onClick(Click click, boolean doubleClick) {
            if (click.button() == 1) {
                if (ArmorConfig.INSTANCE.hasPreset(index)) {
                    ArmorConfig.INSTANCE.deletePreset(index);
                }
                updateDisplay();
                return;
            }

            if (ArmorConfig.INSTANCE.hasPreset(index)) {
                ArmorConfig.INSTANCE.applyPreset(index);
                clearAndInit();
            } else {
                ArmorConfig.INSTANCE.savePreset(index);
                updateDisplay();
            }
        }

        @Override
        protected boolean isValidClickButton(MouseInput input) {
            return input.button() == 0 || input.button() == 1;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            boolean populated = ArmorConfig.INSTANCE.hasPreset(index);
            int background = !active ? 0xFF3F3F3F : isHovered() ? 0xFF707070 : 0xFF555555;
            int highlight = !active ? 0xFF686868 : 0xFFAAAAAA;
            int shadow = !active ? 0xFF202020 : 0xFF373737;

            context.fill(getX(), getY(), getRight(), getBottom(), 0xFF000000);
            context.fill(getX() + 1, getY() + 1, getRight() - 1, getBottom() - 1, background);
            context.fill(getX() + 1, getY() + 1, getRight() - 1, getY() + 2, highlight);
            context.fill(getX() + 1, getY() + 1, getX() + 2, getBottom() - 1, highlight);
            context.fill(getX() + 1, getBottom() - 2, getRight() - 1, getBottom() - 1, shadow);
            context.fill(getRight() - 2, getY() + 1, getRight() - 1, getBottom() - 1, shadow);

            context.drawCenteredTextWithShadow(textRenderer, getMessage(),
                    getX() + getWidth() / 2, getY() + (getHeight() - 18) / 2, 0xFFFFFFFF);
            Text status = getWidth() < 54 ? Text.empty()
                    : Text.translatable(populated ? "armorvisibility.preset.saved" : "armorvisibility.preset.save");
            context.drawCenteredTextWithShadow(textRenderer, status,
                    getX() + getWidth() / 2, getY() + (getHeight() - 18) / 2 + 11,
                    populated ? 0xFFFFFFFF : 0xFFDDDDDD);
        }

        @Override
        protected void appendClickableNarrations(
                net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder
        ) {
            appendDefaultNarrations(builder);
        }
    }
}
