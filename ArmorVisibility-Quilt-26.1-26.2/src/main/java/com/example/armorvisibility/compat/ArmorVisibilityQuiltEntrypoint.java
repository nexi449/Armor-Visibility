package com.example.armorvisibility.compat;

import com.example.armorvisibility.ArmorVisibilityClient;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.entrypoint.PreLaunchEntrypoint;

public final class ArmorVisibilityQuiltEntrypoint implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch(ModContainer mod) {
        new ArmorVisibilityClient().onInitializeClient();
    }
}