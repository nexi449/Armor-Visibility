# Troubleshooting

## The mod does not load

- Start Minecraft with the loader that matches the mod file: Fabric, Quilt, or NeoForge.
- Check that the mod file matches your exact Minecraft version and loader.
- Remove duplicate or incompatible Armor Visibility files from the `mods` folder.
- Install the loader and Java versions required by your Minecraft version.

## I cannot find the settings

- Open the pause menu and look for the Armor Visibility icon beside **Statistics**.
- On Fabric or Quilt, if you use Mod Menu, open its mod list and choose Armor Visibility's configuration option.
- On NeoForge, open Armor Visibility's configuration from the mod list.
- Make sure the installed build is for your loader. Mod Menu is optional.

## A setting does not hide armor on another player

Enable **Apply to Others** to apply per-slot opacity and visibility settings to other players. Set a slot's opacity to **0%** to hide that armor piece. **Global Armor: Off** hides armor globally. **Player Only: On** limits the armor rules to player entities.

## Offhand opacity does not affect a special item

The offhand slider covers standard item rendering and several built-in special renderers. Some modded items provide their own renderer and may not follow Minecraft's normal transparency path.

## Armor is still equipped

That is expected. Armor Visibility changes rendering only; it does not remove equipment or change inventory, stats, or gameplay.

## The config file is missing or invalid

The mod creates `config/armorvisibility.json` when it first starts. If the file is invalid, close Minecraft and remove or rename it; the mod will recreate it with default settings on the next launch.
