package com.example.armorvisibility.compat;

import com.example.armorvisibility.ArmorVisibilityConfig;
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
public class ArmorVisibilityConfigScreen extends Screen {
    private final Screen parent;

    private int settingsX;
    private int settingsWidth;
    private int contentWidth;
    private int previewX;
    private int previewWidth;
    private int startY;
    private int presetHeadingY;
    private boolean showPreview;

    public ArmorVisibilityConfigScreen(Screen parent) {
        super(Text.literal("Armor Visibility"));
        this.parent = parent;
        ArmorVisibilityConfig.INSTANCE.load();
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

        presetHeadingY = startY + 204;
        addPresets(startY + 245);

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
            addDrawableChild(new PresetButton(index, x, y, presetWidth, 30));
        }
    }

    private void addArmorControls(String label, ArmorVisibilityConfig.ArmorPart part, int y) {
        int glintWidth = Math.min(58, settingsWidth / 3);
        int gap = 4;
        int sliderWidth = Math.max(0, settingsWidth - glintWidth - gap);
        addOpacitySlider(
                label,
                ArmorVisibilityConfig.INSTANCE.getArmorOpacity(part.getSlot()),
                opacity -> ArmorVisibilityConfig.INSTANCE.setArmorOpacity(part.getSlot(), opacity),
                sliderWidth,
                y
        );

        addDrawableChild(ButtonWidget.builder(glintText(part.getSlot()), button -> {
            EquipmentSlot slot = part.getSlot();
            ArmorVisibilityConfig.INSTANCE.setShowGlint(slot, !ArmorVisibilityConfig.INSTANCE.shouldShowGlint(slot));
            button.setMessage(glintText(slot));
        }).dimensions(settingsX + sliderWidth + gap, y, glintWidth, 20).build());
    }

    private void addOpacitySlider(String label, float opacity, Consumer<Float> setter, int sliderWidth, int y) {
        addDrawableChild(new OpacitySlider(
                settingsX,
                y,
                sliderWidth,
                20,
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
        }).dimensions(x, y, buttonWidth, 20).build());
    }

    private Text toggleText(String label, boolean enabled) {
        return Text.literal(label + ": " + (enabled ? "ON" : "OFF"));
    }

    private Text glintText(EquipmentSlot slot) {
        return Text.literal("Glint: " + (ArmorVisibilityConfig.INSTANCE.shouldShowGlint(slot) ? "ON" : "OFF"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 18, 0xFFFFFF);
        context.drawTextWithShadow(textRenderer, Text.literal("Presets"), settingsX, presetHeadingY, 0xFFFFFF);
        Text presetInstructions = Text.literal("Left-click: save/load | Right-click: delete");
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
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Preview"),
                previewX + previewWidth / 2, panelY + 8, 0xFFFFFF);

        int entitySize = Math.round(Math.min(previewWidth, panelBottom - panelY) * 0.42F);
        InventoryScreen.drawEntity(context, previewX + 1, panelY + 1, panelRight - 1, panelBottom - 1,
                entitySize, 0.0F, mouseX, mouseY, client.player);
    }

    @Override
    public void removed() {
        ArmorVisibilityConfig.INSTANCE.save();
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
            super(x, y, width, height, Text.literal(label), opacity);
            this.label = label;
            this.setter = setter;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Text.literal(label + ": " + Math.round(value * 100.0) + "%"));
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
            boolean populated = ArmorVisibilityConfig.INSTANCE.hasPreset(index);
            setMessage(Text.literal("PRESET " + (index + 1)));
            String details = ArmorVisibilityConfig.INSTANCE.describePreset(index);
            if (populated) {
                details = details.replace("\nRight-click to delete", "\nLeft-click to load\nRight-click to delete");
            }
            setTooltip(Tooltip.of(Text.literal(details)));
        }

        @Override
        public void onClick(Click click, boolean doubleClick) {
            if (click.button() == 1) {
                if (ArmorVisibilityConfig.INSTANCE.hasPreset(index)) {
                    ArmorVisibilityConfig.INSTANCE.deletePreset(index);
                }
                updateDisplay();
                return;
            }

            if (ArmorVisibilityConfig.INSTANCE.hasPreset(index)) {
                ArmorVisibilityConfig.INSTANCE.applyPreset(index);
                clearAndInit();
            } else {
                ArmorVisibilityConfig.INSTANCE.savePreset(index);
                updateDisplay();
            }
        }

        @Override
        protected boolean isValidClickButton(MouseInput input) {
            return input.button() == 0 || input.button() == 1;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            boolean populated = ArmorVisibilityConfig.INSTANCE.hasPreset(index);
            int background = !active ? 0xFF242424
                    : isHovered() ? (populated ? 0xFF40589A : 0xFF383D4C)
                    : (populated ? 0xFF303D62 : 0xFF292D3A);
            context.fill(getX(), getY(), getRight(), getBottom(), 0xFF111111);
            context.fill(getX() + 1, getY() + 1, getRight() - 1, getBottom() - 1, background);
            context.fill(getX() + 1, getY() + 1, getRight() - 1, getY() + 3,
                    populated ? 0xFF7FA4FF : 0xFF626A7C);
            context.drawCenteredTextWithShadow(textRenderer, getMessage(),
                    getX() + getWidth() / 2, getY() + 6, 0xFFFFFFFF);
            Text status = Text.literal(populated ? "SAVED" : "+ SAVE");
            context.drawCenteredTextWithShadow(textRenderer, status,
                    getX() + getWidth() / 2, getY() + 17, populated ? 0xFFB9CBFF : 0xFFBFC3CC);
        }

        @Override
        protected void appendClickableNarrations(
                net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder
        ) {
            appendDefaultNarrations(builder);
        }
    }
}
