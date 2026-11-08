package com.example.armorvisibility.compat;

import com.example.armorvisibility.ArmorClient;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.entrypoint.PreLaunchEntrypoint;

public final class QuiltEntrypoint implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch(ModContainer mod) {
        new ArmorClient().onInitializeClient();
    }
}