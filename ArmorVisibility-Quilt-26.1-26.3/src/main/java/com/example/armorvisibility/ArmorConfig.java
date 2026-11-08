package com.example.armorvisibility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.EquipmentSlot;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;


public final class ArmorConfig {

    public enum ArmorPart {
        HELMET("Helmet", EquipmentSlot.HEAD),
        CHESTPLATE("Chestplate", EquipmentSlot.CHEST),
        LEGGINGS("Leggings", EquipmentSlot.LEGS),
        BOOTS("Boots", EquipmentSlot.FEET);

        private final String label;
        private final EquipmentSlot slot;

        ArmorPart(String label, EquipmentSlot slot) {
            this.label = label;
            this.slot = slot;
        }

        public String getLabel() {
            return label;
        }

        public EquipmentSlot getSlot() {
            return slot;
        }
    }

    public static final ArmorConfig INSTANCE = new ArmorConfig();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "armorvisibility.json";

    private static final class Data {
        boolean armorVisible = true;
        boolean hideHelmet = false;
        boolean hideChestplate = false;
        boolean hideLeggings = false;
        boolean hideBoots = false;
        Float helmetOpacity = 1.0F;
        Float chestplateOpacity = 1.0F;
        Float leggingsOpacity = 1.0F;
        Float bootsOpacity = 1.0F;
        Float offhandOpacity = 1.0F;
        boolean helmetGlint = true;
        boolean chestplateGlint = true;
        boolean leggingsGlint = true;
        boolean bootsGlint = true;
        boolean hideForOtherPlayers = false;
        boolean keepElytraVisible = true;
        boolean keepCapeVisible = true;
        boolean playerOnly = false;
        Preset[] presets = new Preset[5];
    }

    private static final class Preset {
        boolean armorVisible;
        boolean hideHelmet;
        boolean hideChestplate;
        boolean hideLeggings;
        boolean hideBoots;
        Float helmetOpacity;
        Float chestplateOpacity;
        Float leggingsOpacity;
        Float bootsOpacity;
        Float offhandOpacity;
        boolean helmetGlint;
        boolean chestplateGlint;
        boolean leggingsGlint;
        boolean bootsGlint;
        boolean hideForOtherPlayers;
        boolean keepElytraVisible;
        boolean keepCapeVisible;
        boolean playerOnly;
    }

    private final Path configPath;
    private Data data;

    private ArmorConfig() {
        this.configPath = FabricLoader.getInstance()
                .getConfigDir()
                .resolve(FILE_NAME);
        this.data = new Data();
    }


    public synchronized void load() {
        if (!Files.exists(configPath)) {

            this.data = new Data();
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
            Data loaded = GSON.fromJson(reader, Data.class);
            this.data = (loaded != null) ? loaded : new Data();
            if (this.data.presets == null) {
                this.data.presets = new Preset[5];
            } else if (this.data.presets.length != 5) {
                this.data.presets = Arrays.copyOf(this.data.presets, 5);
            }
        } catch (IOException | JsonSyntaxException e) {


            System.err.println("[ArmorVisibility] Failed to read config, falling back to defaults: " + e.getMessage());
            this.data = new Data();
        }
    }


    public synchronized void save() {
        try {
            Path parent = configPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (Writer writer = Files.newBufferedWriter(configPath, StandardCharsets.UTF_8)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            System.err.println("[ArmorVisibility] Failed to save config: " + e.getMessage());
        }
    }

    /**
     * Returns whether armor is visible globally.
     *
     * @return true if armor is visible
     */
    public synchronized boolean isArmorVisible() {
        return data.armorVisible;
    }

    /**
     * Set whether armor is visible globally and persist config.
     *
     * @param visible the new value
     */
    public synchronized void setArmorVisible(boolean visible) {
        data.armorVisible = visible;
        save();
    }

    public synchronized boolean shouldHideForOtherPlayers() {
        return data.hideForOtherPlayers;
    }

    public synchronized void setHideForOtherPlayers(boolean hideForOtherPlayers) {
        data.hideForOtherPlayers = hideForOtherPlayers;
        save();
    }

    public synchronized boolean shouldKeepElytraVisible() {
        return data.keepElytraVisible;
    }

    public synchronized void setKeepElytraVisible(boolean keepElytraVisible) {
        data.keepElytraVisible = keepElytraVisible;
        save();
    }

    public synchronized boolean shouldKeepCapeVisible() {
        return data.keepCapeVisible;
    }

    public synchronized void setKeepCapeVisible(boolean keepCapeVisible) {
        data.keepCapeVisible = keepCapeVisible;
        save();
    }

    public synchronized boolean isPlayerOnly() {
        return data.playerOnly;
    }

    public synchronized void setPlayerOnly(boolean playerOnly) {
        data.playerOnly = playerOnly;
        save();
    }

    public synchronized boolean isArmorPartHidden(EquipmentSlot slot) {
        if (!data.armorVisible) {
            return true;
        }

        return switch (slot) {
            case HEAD -> data.hideHelmet || getArmorOpacity(slot) == 0.0F;
            case CHEST -> data.hideChestplate || getArmorOpacity(slot) == 0.0F;
            case LEGS -> data.hideLeggings || getArmorOpacity(slot) == 0.0F;
            case FEET -> data.hideBoots || getArmorOpacity(slot) == 0.0F;
            default -> false;
        };
    }

    public synchronized void setArmorPartHidden(EquipmentSlot slot, boolean hidden) {
        switch (slot) {
            case HEAD -> data.hideHelmet = hidden;
            case CHEST -> data.hideChestplate = hidden;
            case LEGS -> data.hideLeggings = hidden;
            case FEET -> data.hideBoots = hidden;
            default -> {
            }
        }
        save();
    }

    public synchronized float getArmorOpacity(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> data.hideHelmet ? 0.0F : clampOpacity(data.helmetOpacity);
            case CHEST -> data.hideChestplate ? 0.0F : clampOpacity(data.chestplateOpacity);
            case LEGS -> data.hideLeggings ? 0.0F : clampOpacity(data.leggingsOpacity);
            case FEET -> data.hideBoots ? 0.0F : clampOpacity(data.bootsOpacity);
            default -> 1.0F;
        };
    }

    public synchronized void setArmorOpacity(EquipmentSlot slot, float opacity) {
        float clampedOpacity = clampOpacity(opacity);
        switch (slot) {
            case HEAD -> {
                data.hideHelmet = false;
                data.helmetOpacity = clampedOpacity;
            }
            case CHEST -> {
                data.hideChestplate = false;
                data.chestplateOpacity = clampedOpacity;
            }
            case LEGS -> {
                data.hideLeggings = false;
                data.leggingsOpacity = clampedOpacity;
            }
            case FEET -> {
                data.hideBoots = false;
                data.bootsOpacity = clampedOpacity;
            }
            default -> {
                return;
            }
        }
    }

    public synchronized float getOffhandOpacity() {
        return clampOpacity(data.offhandOpacity);
    }

    public synchronized void setOffhandOpacity(float opacity) {
        data.offhandOpacity = clampOpacity(opacity);
    }

    public synchronized boolean shouldShowGlint(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> data.helmetGlint;
            case CHEST -> data.chestplateGlint;
            case LEGS -> data.leggingsGlint;
            case FEET -> data.bootsGlint;
            default -> true;
        };
    }

    public synchronized void setShowGlint(EquipmentSlot slot, boolean showGlint) {
        switch (slot) {
            case HEAD -> data.helmetGlint = showGlint;
            case CHEST -> data.chestplateGlint = showGlint;
            case LEGS -> data.leggingsGlint = showGlint;
            case FEET -> data.bootsGlint = showGlint;
            default -> {
                return;
            }
        }
        save();
    }

    public synchronized boolean hasPreset(int index) {
        checkPresetIndex(index);
        return data.presets[index] != null;
    }

    public synchronized void savePreset(int index) {
        checkPresetIndex(index);
        Preset preset = new Preset();
        preset.armorVisible = data.armorVisible;
        preset.hideHelmet = data.hideHelmet;
        preset.hideChestplate = data.hideChestplate;
        preset.hideLeggings = data.hideLeggings;
        preset.hideBoots = data.hideBoots;
        preset.helmetOpacity = clampOpacity(data.helmetOpacity);
        preset.chestplateOpacity = clampOpacity(data.chestplateOpacity);
        preset.leggingsOpacity = clampOpacity(data.leggingsOpacity);
        preset.bootsOpacity = clampOpacity(data.bootsOpacity);
        preset.offhandOpacity = clampOpacity(data.offhandOpacity);
        preset.helmetGlint = data.helmetGlint;
        preset.chestplateGlint = data.chestplateGlint;
        preset.leggingsGlint = data.leggingsGlint;
        preset.bootsGlint = data.bootsGlint;
        preset.hideForOtherPlayers = data.hideForOtherPlayers;
        preset.keepElytraVisible = data.keepElytraVisible;
        preset.keepCapeVisible = data.keepCapeVisible;
        preset.playerOnly = data.playerOnly;
        data.presets[index] = preset;
        save();
    }

    public synchronized void applyPreset(int index) {
        checkPresetIndex(index);
        Preset preset = data.presets[index];
        if (preset == null) {
            return;
        }

        data.armorVisible = preset.armorVisible;
        data.hideHelmet = preset.hideHelmet;
        data.hideChestplate = preset.hideChestplate;
        data.hideLeggings = preset.hideLeggings;
        data.hideBoots = preset.hideBoots;
        data.helmetOpacity = clampOpacity(preset.helmetOpacity);
        data.chestplateOpacity = clampOpacity(preset.chestplateOpacity);
        data.leggingsOpacity = clampOpacity(preset.leggingsOpacity);
        data.bootsOpacity = clampOpacity(preset.bootsOpacity);
        data.offhandOpacity = clampOpacity(preset.offhandOpacity);
        data.helmetGlint = preset.helmetGlint;
        data.chestplateGlint = preset.chestplateGlint;
        data.leggingsGlint = preset.leggingsGlint;
        data.bootsGlint = preset.bootsGlint;
        data.hideForOtherPlayers = preset.hideForOtherPlayers;
        data.keepElytraVisible = preset.keepElytraVisible;
        data.keepCapeVisible = preset.keepCapeVisible;
        data.playerOnly = preset.playerOnly;
        save();
    }

    public synchronized void deletePreset(int index) {
        checkPresetIndex(index);
        data.presets[index] = null;
        save();
    }

    public synchronized String describePreset(int index) {
        checkPresetIndex(index);
        Preset preset = data.presets[index];
        if (preset == null) {
            return "Empty preset\nLeft-click to save current settings";
        }

        return "Armor: " + onOff(preset.armorVisible)
                + "\nHelmet: " + percentage(preset.hideHelmet ? 0.0F : preset.helmetOpacity)
                + " | Glint: " + onOff(preset.helmetGlint)
                + "\nChestplate: " + percentage(preset.hideChestplate ? 0.0F : preset.chestplateOpacity)
                + " | Glint: " + onOff(preset.chestplateGlint)
                + "\nLeggings: " + percentage(preset.hideLeggings ? 0.0F : preset.leggingsOpacity)
                + " | Glint: " + onOff(preset.leggingsGlint)
                + "\nBoots: " + percentage(preset.hideBoots ? 0.0F : preset.bootsOpacity)
                + " | Glint: " + onOff(preset.bootsGlint)
                + "\nOffhand: " + percentage(preset.offhandOpacity)
                + "\nCape: " + onOff(preset.keepCapeVisible)
                + " | Elytra: " + onOff(preset.keepElytraVisible)
                + "\nPlayer only: " + onOff(preset.playerOnly)
                + " | Others: " + onOff(preset.hideForOtherPlayers)
                + "\nRight-click to delete";
    }

    private static void checkPresetIndex(int index) {
        if (index < 0 || index >= 5) {
            throw new IndexOutOfBoundsException("Preset index must be between 0 and 4: " + index);
        }
    }

    private static String onOff(boolean enabled) {
        return enabled ? "ON" : "OFF";
    }

    private static String percentage(Float value) {
        return Math.round(clampOpacity(value) * 100.0F) + "%";
    }

    private static float clampOpacity(Float opacity) {
        if (opacity == null) {
            return 1.0F;
        }
        return Math.max(0.0F, Math.min(1.0F, opacity));
    }
}
