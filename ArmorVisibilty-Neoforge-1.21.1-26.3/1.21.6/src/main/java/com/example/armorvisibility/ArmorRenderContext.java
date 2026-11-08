package com.example.armorvisibility;

import net.minecraft.world.entity.EquipmentSlot;

public final class ArmorRenderContext {
    private static final ThreadLocal<State> CURRENT_STATE = new ThreadLocal<>();
    private static final ThreadLocal<Float> OFFHAND_OPACITY = new ThreadLocal<>();

    private ArmorRenderContext() {
    }

    public static EquipmentSlot getCurrentSlot() {
        State state = CURRENT_STATE.get();
        return state == null ? null : state.slot();
    }

    public static float getCurrentOpacity() {
        State state = CURRENT_STATE.get();
        return state == null ? 1.0F : state.opacity();
    }

    public static boolean shouldApplySettings() {
        State state = CURRENT_STATE.get();
        return state != null && state.applySettings();
    }

    public static State setCurrentState(EquipmentSlot slot, float opacity, boolean applySettings) {
        State previous = CURRENT_STATE.get();
        CURRENT_STATE.set(new State(slot, opacity, applySettings));
        return previous;
    }

    public static void restoreState(State state) {
        if (state == null) {
            CURRENT_STATE.remove();
        } else {
            CURRENT_STATE.set(state);
        }
    }

    public static Float setOffhandOpacity(float opacity) {
        Float previousOpacity = OFFHAND_OPACITY.get();
        OFFHAND_OPACITY.set(opacity);
        return previousOpacity;
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

    public record State(EquipmentSlot slot, float opacity, boolean applySettings) {
    }
}
