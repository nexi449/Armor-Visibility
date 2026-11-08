package com.example.armorvisibility.compat;

import com.example.armorvisibility.ArmorConfig;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
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
        super(Component.translatable("armorvisibility.screen.title"));
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
                .replace("Empty preset", Component.translatable("armorvisibility.preset.empty").getString())
                .replace("Click to save current settings", Component.translatable("armorvisibility.preset.save_hint").getString())
                .replace("Left-click to save current settings", Component.translatable("armorvisibility.preset.save_hint").getString())
                .replace("Armor", Component.translatable("armorvisibility.preset.armor").getString())
                .replace("Helmet", Component.translatable("armorvisibility.preset.helmet").getString())
                .replace("Chestplate", Component.translatable("armorvisibility.preset.chestplate").getString())
                .replace("Leggings", Component.translatable("armorvisibility.preset.leggings").getString())
                .replace("Boots", Component.translatable("armorvisibility.preset.boots").getString())
                .replace("Glint", Component.translatable("armorvisibility.preset.glint").getString())
                .replace("Offhand", Component.translatable("armorvisibility.preset.offhand").getString())
                .replace("Cape", Component.translatable("armorvisibility.preset.cape").getString())
                .replace("Elytra", Component.translatable("armorvisibility.preset.elytra").getString())
                .replace("Player only", Component.translatable("armorvisibility.preset.player_only").getString())
                .replace("Others", Component.translatable("armorvisibility.preset.others").getString())
                .replace("Left-click to load", Component.translatable("armorvisibility.preset.load_hint").getString())
                .replace("Right-click to delete", Component.translatable("armorvisibility.preset.delete_hint").getString())
                .replace("ON", Component.translatable("armorvisibility.state.on").getString())
                .replace("OFF", Component.translatable("armorvisibility.state.off").getString());
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
        return Component.translatable("armorvisibility.setting.toggle", Component.translatable(label), Component.translatable(enabled ? "armorvisibility.state.on" : "armorvisibility.state.off"));
    }

    private Component glintText(EquipmentSlot slot) {
        return Component.translatable("armorvisibility.setting.glint", Component.translatable(ArmorConfig.INSTANCE.shouldShowGlint(slot) ? "armorvisibility.state.on" : "armorvisibility.state.off"));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(font, title, width / 2, 18, 0xFFFFFF);
        graphics.text(font, Component.translatable("armorvisibility.presets.title"), settingsX, layoutY(204), 0xFFFFFF);
        Component instructions = Component.translatable("armorvisibility.presets.instructions");
        graphics.text(font, instructions, settingsX + contentWidth - font.width(instructions),
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
        graphics.centeredText(font, Component.translatable("armorvisibility.preview.title"),
                previewX + previewWidth / 2, panelY + 8, 0xFFFFFF);

        int entitySize = Math.round(Math.min(previewWidth, panelBottom - panelY) * 0.42F);
        InventoryScreen.extractEntityInInventoryFollowsMouse(graphics, previewX + 1, panelY + 1,
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

    private void handlePresetClick(int index,int button){boolean populated=ArmorConfig.INSTANCE.hasPreset(index);
        if(button==InputConstants.MOUSE_BUTTON_RIGHT){if(populated)ArmorConfig.INSTANCE.deletePreset(index);}
        else if(button==InputConstants.MOUSE_BUTTON_LEFT&&populated)ArmorConfig.INSTANCE.applyPreset(index);
        else if(button==InputConstants.MOUSE_BUTTON_LEFT)ArmorConfig.INSTANCE.savePreset(index);rebuildWidgets();}

    private final class PresetButton extends AbstractWidget {
        private final int index;
        private PresetButton(int index,int x,int y,int width,int height){super(x,y,width,height,Component.empty());this.index=index;setTooltipDelay(java.time.Duration.ofMillis(250));updateDisplay();}
        private void updateDisplay(){boolean populated=ArmorConfig.INSTANCE.hasPreset(index);Component presetTitle = Component.translatable("armorvisibility.preset.title", index + 1);
            setMessage(getWidth() < font.width(presetTitle) + 8
                    ? Component.translatable("armorvisibility.preset.short", index + 1)
                    : presetTitle);
            String details=ArmorConfig.INSTANCE.describePreset(index);if(populated)details=details.replace("\nRight-click to delete","\nLeft-click to load\nRight-click to delete");setTooltip(Tooltip.create(Component.literal(localizePresetDescription(details))));}
        @Override public void onClick(MouseButtonEvent event,boolean doubleClick){handlePresetClick(index,event.button());}
        @Override protected boolean isValidClickButton(MouseButtonInfo button){return button.button()==InputConstants.MOUSE_BUTTON_LEFT||button.button()==InputConstants.MOUSE_BUTTON_RIGHT;}
        @Override protected void extractWidgetRenderState(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float delta){
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

            graphics.centeredText(font, getMessage(), getX() + getWidth() / 2,
                    getY() + (getHeight() - 18) / 2, 0xFFFFFFFF);
            graphics.centeredText(font, (getWidth() < 54 ? Component.empty() : Component.translatable(
                    populated ? "armorvisibility.preset.saved" : "armorvisibility.preset.save")),
                    getX() + getWidth() / 2, getY() + (getHeight() - 18) / 2 + 11, 0xFFDDDDDD);
            handleCursor(graphics);
}
        @Override public void updateWidgetNarration(NarrationElementOutput output){defaultButtonNarrationText(output);}
    }

    @Override
    public void removed() {
        ArmorConfig.INSTANCE.save();
        super.removed();
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreenAndShow(parent);
    }

    private static final class OpacitySlider extends AbstractSliderButton {
        private final String label;
        private final Consumer<Float> setter;

        private OpacitySlider(int x, int y, int width, int height, String label,
                              float opacity, Consumer<Float> setter) {
            super(x, y, width, height, Component.translatable(label), opacity);
            this.label = label;
            this.setter = setter;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable("armorvisibility.setting.opacity", Component.translatable(label), Math.round(value * 100.0)));
        }

        @Override
        protected void applyValue() {
            setter.accept((float) value);
        }
    }
}
