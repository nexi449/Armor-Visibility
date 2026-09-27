package com.example.armorvisibility.compat;

import com.example.armorvisibility.ArmorVisibilityConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class ArmorVisibilityConfigScreen extends Screen {
    private final Screen parent;

    public ArmorVisibilityConfigScreen(Screen parent) {
        super(Text.literal("Armor Visibility"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        ArmorVisibilityConfig.INSTANCE.load();

        int contentWidth = Math.min(368, Math.max(0, this.width - 32));
        int columnGap = 8;
        int buttonWidth = (contentWidth - columnGap) / 2;
        int leftX = (this.width - contentWidth) / 2;
        int rightX = leftX + buttonWidth + columnGap;
        int contentHeight = 140;
        int startY = Math.max(48, (this.height - contentHeight) / 2);

        addArmorToggle("Helmet", ArmorVisibilityConfig.ArmorPart.HELMET, leftX, startY, buttonWidth);
        addArmorToggle("Chestplate", ArmorVisibilityConfig.ArmorPart.CHESTPLATE, rightX, startY, buttonWidth);
        addArmorToggle("Leggings", ArmorVisibilityConfig.ArmorPart.LEGGINGS, leftX, startY + 28, buttonWidth);
        addArmorToggle("Boots", ArmorVisibilityConfig.ArmorPart.BOOTS, rightX, startY + 28, buttonWidth);

        addDrawableChild(createToggleButton(
                "Global Armor",
                ArmorVisibilityConfig.INSTANCE::isArmorVisible,
                ArmorVisibilityConfig.INSTANCE::setArmorVisible,
                leftX + (contentWidth - buttonWidth) / 2,
                startY + 56,
                buttonWidth
        ));

        addDrawableChild(createToggleButton(
                "Cape",
                ArmorVisibilityConfig.INSTANCE::shouldKeepCapeVisible,
                ArmorVisibilityConfig.INSTANCE::setKeepCapeVisible,
                leftX,
                startY + 92,
                buttonWidth
        ));
        addDrawableChild(createToggleButton(
                "Elytra",
                ArmorVisibilityConfig.INSTANCE::shouldKeepElytraVisible,
                ArmorVisibilityConfig.INSTANCE::setKeepElytraVisible,
                rightX,
                startY + 92,
                buttonWidth
        ));
        addDrawableChild(createToggleButton(
                "Player Only",
                ArmorVisibilityConfig.INSTANCE::isPlayerOnly,
                ArmorVisibilityConfig.INSTANCE::setPlayerOnly,
                leftX,
                startY + 120,
                buttonWidth
        ));
        addDrawableChild(createToggleButton(
                "Other Armor",
                ArmorVisibilityConfig.INSTANCE::shouldHideForOtherPlayers,
                ArmorVisibilityConfig.INSTANCE::setHideForOtherPlayers,
                rightX,
                startY + 120,
                buttonWidth
        ));

        int doneWidth = Math.min(200, Math.max(0, this.width - 32));
        addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> client.setScreen(parent))
                .dimensions((this.width - doneWidth) / 2, this.height - 32, doneWidth, 20)
                .build());
    }

    private void addArmorToggle(String label, ArmorVisibilityConfig.ArmorPart part, int x, int y, int buttonWidth) {
        addDrawableChild(ButtonWidget.builder(armorPartText(label, part), button -> {
            ArmorVisibilityConfig.INSTANCE.setArmorPartHidden(
                    part.getSlot(),
                    !ArmorVisibilityConfig.INSTANCE.isArmorPartHidden(part.getSlot())
            );
            clearAndInit();
        }).dimensions(x, y, buttonWidth, 20).build());
    }

    private ButtonWidget createToggleButton(String label, java.util.function.BooleanSupplier state,
                                            java.util.function.Consumer<Boolean> setter, int x, int y, int buttonWidth) {
        return ButtonWidget.builder(toggleText(label, state.getAsBoolean()), button -> {
            setter.accept(!state.getAsBoolean());
            clearAndInit();
        }).dimensions(x, y, buttonWidth, 20).build();
    }

    private Text armorPartText(String label, ArmorVisibilityConfig.ArmorPart part) {
        boolean hidden = ArmorVisibilityConfig.INSTANCE.isArmorPartHidden(part.getSlot());
        return Text.literal(label + ": " + (hidden ? "Hidden" : "Shown"));
    }

    private Text toggleText(String label, boolean enabled) {
        if (label.equals("Other Armor")) {
            return Text.literal(label + ": " + (enabled ? "Hidden" : "Visible"));
        }
        return Text.literal(label + ": " + (enabled ? "On" : "Off"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 20, 0xFFFFFF);
    }
}
