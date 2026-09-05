package com.example.armorvisibility.compat;

import com.example.armorvisibility.ArmorVisibilityConfig;
import com.example.armorvisibility.ArmorVisibilityClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
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

        GridLayout grid = new GridLayout();
        grid.spacing(8);
        GridLayout.RowHelper adder = grid.createRowHelper(1);

        Button globalButton = Button.builder(Component.literal("Global: " + (ArmorVisibilityConfig.INSTANCE.isArmorVisible() ? "On" : "Off")), button -> {
            ArmorVisibilityConfig.INSTANCE.setArmorVisible(!ArmorVisibilityConfig.INSTANCE.isArmorVisible());
            rebuildWidgets();
        }).build();
        adder.addChild(globalButton);
        Button elytraButton = createVisibilityButton(
            "Elytra",
            ArmorVisibilityConfig.INSTANCE::shouldKeepElytraVisible,
            visible -> ArmorVisibilityConfig.INSTANCE.setKeepElytraVisible(visible)
        );

        Button othersButton = Button.builder(Component.literal("Hide Armor of Others: " + (ArmorVisibilityConfig.INSTANCE.shouldHideForOtherPlayers() ? "On" : "Off")), button -> {
            ArmorVisibilityConfig.INSTANCE.setHideForOtherPlayers(!ArmorVisibilityConfig.INSTANCE.shouldHideForOtherPlayers());
            rebuildWidgets();
        }).build();
        adder.addChild(othersButton);
        Button capeButton = createVisibilityButton(
            "Cape",
            ArmorVisibilityConfig.INSTANCE::shouldKeepCapeVisible,
            visible -> ArmorVisibilityConfig.INSTANCE.setKeepCapeVisible(visible)
        );
        Button playerOnlyButton = createVisibilityButton(
            "Player Only",
            ArmorVisibilityConfig.INSTANCE::isPlayerOnly,
            visible -> ArmorVisibilityConfig.INSTANCE.setPlayerOnly(visible)
        );

        addToggle(adder, "Helmet", ArmorVisibilityConfig.ArmorPart.HELMET);
        addToggle(adder, "Chestplate", ArmorVisibilityConfig.ArmorPart.CHESTPLATE);
        addToggle(adder, "Leggings", ArmorVisibilityConfig.ArmorPart.LEGGINGS);
        addToggle(adder, "Boots", ArmorVisibilityConfig.ArmorPart.BOOTS);

        Button doneButton = Button.builder(Component.translatable("gui.done"), button -> ArmorVisibilityClient.showScreen(Minecraft.getInstance(), parent)).build();
        adder.addChild(doneButton);

        grid.arrangeElements();
        grid.setX(width - grid.getWidth() - 60);
        grid.setY(40 + (height - 80 - grid.getHeight()) / 2);
        grid.visitWidgets(this::addRenderableWidget);

        elytraButton.setPosition(globalButton.getX() - elytraButton.getWidth() - 8, globalButton.getY() + 6);
        capeButton.setPosition(othersButton.getX() - capeButton.getWidth() - 8, othersButton.getY() + 6);
        playerOnlyButton.setPosition(capeButton.getX(), capeButton.getY() + capeButton.getHeight() + 8);
        addRenderableWidget(elytraButton);
        addRenderableWidget(capeButton);
        addRenderableWidget(playerOnlyButton);
    }

    private void addToggle(GridLayout.RowHelper adder, String label, ArmorVisibilityConfig.ArmorPart part) {
        boolean hidden = ArmorVisibilityConfig.INSTANCE.isArmorPartHidden(part.getSlot());
        Button toggle = Button.builder(
            Component.literal(label + ": " + (hidden ? "Hidden" : "Shown")),
                button -> {
                    boolean newValue = !ArmorVisibilityConfig.INSTANCE.isArmorPartHidden(part.getSlot());
                    ArmorVisibilityConfig.INSTANCE.setArmorPartHidden(part.getSlot(), newValue);
                    rebuildWidgets();
                }
        ).build();
        adder.addChild(toggle);
    }

    private Button createVisibilityButton(String label, java.util.function.BooleanSupplier state,
                                                java.util.function.Consumer<Boolean> setter) {
        String enabledSymbol = label.equals("Player Only") ? "↔" : "↑";
        boolean enabled = state.getAsBoolean();
        Button button = Button.builder(Component.literal(enabled ? enabledSymbol : "↓"), clicked -> {
            boolean newValue = !state.getAsBoolean();
            setter.accept(newValue);
            rebuildWidgets();
        }).bounds(0, 0, 20, 20).build();
        button.setTooltip(Tooltip.create(Component.literal(label + (enabled ? ": on" : ": off"))));
        return button;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        context.centeredText(font, title, width / 2, 20, 0xFFFFFF);
    }
}
