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


public final class ArmorVisibilityConfig {

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

    public static final ArmorVisibilityConfig INSTANCE = new ArmorVisibilityConfig();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "armorvisibility.json";

    private static final class Data {
        boolean armorVisible = true;
        boolean hideHelmet = false;
        boolean hideChestplate = false;
        boolean hideLeggings = false;
        boolean hideBoots = false;
        boolean hideForOtherPlayers = false;
        boolean keepElytraVisible = true;
        boolean keepCapeVisible = true;
        boolean playerOnly = false;
    }

    private final Path configPath;
    private Data data;

    private ArmorVisibilityConfig() {
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
            case HEAD -> data.hideHelmet;
            case CHEST -> data.hideChestplate;
            case LEGS -> data.hideLeggings;
            case FEET -> data.hideBoots;
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
}
