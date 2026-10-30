package com.example.armorvisibility;

import com.example.armorvisibility.compat.ArmorVisibilityConfigScreen;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(ArmorVisibilityMod.MOD_ID)
public final class ArmorVisibilityMod {
    public static final String MOD_ID = "armorvisibility";

    public ArmorVisibilityMod(ModContainer modContainer) {
        ArmorVisibilityConfig.INSTANCE.load();
        IConfigScreenFactory configScreenFactory =
                (container, parent) -> new ArmorVisibilityConfigScreen(parent);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, configScreenFactory);
    }
}
