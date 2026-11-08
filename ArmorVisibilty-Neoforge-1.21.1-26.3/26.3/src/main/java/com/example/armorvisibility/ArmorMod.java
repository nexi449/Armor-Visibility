package com.example.armorvisibility;

import com.example.armorvisibility.compat.ConfigScreen;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(ArmorMod.MOD_ID)
public final class ArmorMod {
    public static final String MOD_ID = "armorvisibility";

    public ArmorMod(ModContainer modContainer) {
        ArmorConfig.INSTANCE.load();
        IConfigScreenFactory configScreenFactory =
                (container, parent) -> new ConfigScreen(parent);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, configScreenFactory);
    }
}
