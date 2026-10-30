package com.example.armorvisibility;

import net.minecraft.world.entity.EquipmentSlot;

public final class ArmorVisibilityRenderContext {
    private static final ThreadLocal<ArmorState> ARMOR_STATE = new ThreadLocal<>();
    private static final ThreadLocal<Float> OFFHAND_OPACITY = new ThreadLocal<>();

    private ArmorVisibilityRenderContext() {
    }

    public static ArmorState setArmorState(EquipmentSlot slot, float opacity, boolean applySettings) {
        ArmorState previous = ARMOR_STATE.get();
        ARMOR_STATE.set(new ArmorState(slot, opacity, applySettings));
        return previous;
    }

    public static ArmorState getArmorState() {
        return ARMOR_STATE.get();
    }

    public static void restoreArmorState(ArmorState state) {
        if (state == null) {
            ARMOR_STATE.remove();
        } else {
            ARMOR_STATE.set(state);
        }
    }

    public static Float setOffhandOpacity(float opacity) {
        Float previous = OFFHAND_OPACITY.get();
        OFFHAND_OPACITY.set(opacity);
        return previous;
    }

    public static boolean hasOffhandOpacity() {
        return OFFHAND_OPACITY.get() != null;
    }

    public static float getOffhandOpacity() {
        Float opacity = OFFHAND_OPACITY.get();
        return opacity == null ? 1.0F : opacity;
    }

    public static void restoreOffhandOpacity(Float opacity) {
        if (opacity == null) {
            OFFHAND_OPACITY.remove();
        } else {
            OFFHAND_OPACITY.set(opacity);
        }
    }

    public record ArmorState(EquipmentSlot slot, float opacity, boolean applySettings) {
    }
}
