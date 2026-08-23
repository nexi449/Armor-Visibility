package com.example.armorvisibility.compat;

import com.example.armorvisibility.ArmorVisibilityConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.client.gui.tooltip.Tooltip;

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

        GridWidget grid = new GridWidget();
        grid.setSpacing(8);
        GridWidget.Adder adder = grid.createAdder(1);

        ButtonWidget globalButton = ButtonWidget.builder(Text.literal("Global: " + (ArmorVisibilityConfig.INSTANCE.isArmorVisible() ? "On" : "Off")), button -> {
            ArmorVisibilityConfig.INSTANCE.setArmorVisible(!ArmorVisibilityConfig.INSTANCE.isArmorVisible());
            button.setMessage(Text.literal("Global: " + (ArmorVisibilityConfig.INSTANCE.isArmorVisible() ? "On" : "Off")));
            updateCheckboxes();
        }).build();
        adder.add(globalButton);
        ButtonWidget elytraButton = createVisibilityButton(
            "Elytra",
            ArmorVisibilityConfig.INSTANCE::isPlayerOnly,
            visible -> ArmorVisibilityConfig.INSTANCE.setKeepElytraVisible(visible)
        );

        ButtonWidget othersButton = ButtonWidget.builder(Text.literal("Hide Armor of Others: " + (ArmorVisibilityConfig.INSTANCE.shouldHideForOtherPlayers() ? "On" : "Off")), button -> {
            ArmorVisibilityConfig.INSTANCE.setHideForOtherPlayers(!ArmorVisibilityConfig.INSTANCE.shouldHideForOtherPlayers());
            button.setMessage(Text.literal("Hide Armor of Others: " + (ArmorVisibilityConfig.INSTANCE.shouldHideForOtherPlayers() ? "On" : "Off")));
        }).build();
        adder.add(othersButton);
        ButtonWidget capeButton = createVisibilityButton(
            "Cape",
            ArmorVisibilityConfig.INSTANCE::shouldKeepCapeVisible,
            visible -> ArmorVisibilityConfig.INSTANCE.setKeepCapeVisible(visible)
        );
        ButtonWidget playerOnlyButton = createVisibilityButton(
            "Player Only",
            ArmorVisibilityConfig.INSTANCE::shouldKeepElytraVisible,
            visible -> ArmorVisibilityConfig.INSTANCE.setPlayerOnly(visible)
        );

        addToggle(adder, "Helmet", ArmorVisibilityConfig.ArmorPart.HELMET);
        addToggle(adder, "Chestplate", ArmorVisibilityConfig.ArmorPart.CHESTPLATE);
        addToggle(adder, "Leggings", ArmorVisibilityConfig.ArmorPart.LEGGINGS);
        addToggle(adder, "Boots", ArmorVisibilityConfig.ArmorPart.BOOTS);

        ButtonWidget doneButton = ButtonWidget.builder(ScreenTexts.DONE, button -> client.setScreen(parent)).build();
        adder.add(doneButton);

        grid.refreshPositions();
        SimplePositioningWidget.setPos(grid, this.width / 2 - 120, 40, this.width - 40, this.height - 80);
        grid.forEachChild(child -> {
            child.setX(this.width - 40 - child.getWidth());
            addDrawableChild(child);
        });

        elytraButton.setPosition(globalButton.getX() - elytraButton.getWidth() - 8, globalButton.getY() + 6);
        capeButton.setPosition(othersButton.getX() - capeButton.getWidth() - 8, othersButton.getY() + 6);
        playerOnlyButton.setPosition(capeButton.getX(), capeButton.getY() + capeButton.getHeight() + 8);
        addDrawableChild(elytraButton);
        addDrawableChild(capeButton);
        addDrawableChild(playerOnlyButton);
    }

    private void addToggle(GridWidget.Adder adder, String label, ArmorVisibilityConfig.ArmorPart part) {
        boolean hidden = ArmorVisibilityConfig.INSTANCE.isArmorPartHidden(part.getSlot());
        ButtonWidget toggle = ButtonWidget.builder(
                Text.literal(label + ": " + (hidden ? "Hidden" : "Shown")),
                button -> {
                    boolean newValue = !ArmorVisibilityConfig.INSTANCE.isArmorPartHidden(part.getSlot());
                    ArmorVisibilityConfig.INSTANCE.setArmorPartHidden(part.getSlot(), newValue);
                    button.setMessage(Text.literal(label + ": " + (newValue ? "Hidden" : "Shown")));
                }
        ).build();
        adder.add(toggle);
    }

    private ButtonWidget createVisibilityButton(String label, java.util.function.BooleanSupplier state,
                                                java.util.function.Consumer<Boolean> setter) {
        String enabledSymbol = label.equals("Player Only") ? "↔" : "↑";
        boolean enabled = state.getAsBoolean();
        ButtonWidget button = ButtonWidget.builder(Text.literal(enabled ? enabledSymbol : "↓"), clicked -> {
            boolean newValue = !state.getAsBoolean();
            setter.accept(newValue);
            clicked.setMessage(Text.literal(newValue ? enabledSymbol : "↓"));
            clicked.setTooltip(Tooltip.of(Text.literal(label + (newValue ? ": on" : ": off"))));
            updateCheckboxes();
        }).dimensions(0, 0, 20, 20).build();
        button.setTooltip(Tooltip.of(Text.literal(label + (enabled ? ": on" : ": off"))));
        return button;
    }

    private void updateCheckboxes() {
        clearAndInit();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 20, 0xFFFFFF);
    }
}
