package com.example.armorvisibility;

import net.minecraftforge.fml.common.Mod;

@Mod(ArmorMod.MOD_ID)
public final class ArmorMod {
    public static final String MOD_ID = "armorvisibility";

    public ArmorMod() {
        ArmorConfig.INSTANCE.load();
    }
}
