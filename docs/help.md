# Troubleshooting

## The mod does not load

- Start Minecraft with the loader that matches the mod file: Fabric, Quilt, or NeoForge.
- Check that the mod file matches your exact Minecraft version and loader.
- Remove duplicate or incompatible Armor Visibility files from the `mods` folder.
- Install the loader and Java versions required by your Minecraft version.

## I cannot find the settings

- Open the pause menu and look for the Armor Visibility icon beside **Statistics**.
- If you use Mod Menu, open its mod list and choose Armor Visibility's configuration option.
- Make sure the installed build is for your loader. Mod Menu is optional.

## A setting does not hide armor on another player

Enable **Other Armor** to apply per-slot hide settings to other players. **Global Armor: Off** hides armor globally. **Player Only: On** limits the armor rules to player entities.

## Armor is still equipped

That is expected. Armor Visibility changes rendering only; it does not remove equipment or change inventory, stats, or gameplay.

## The config file is missing or invalid

The mod creates `config/armorvisibility.json` when it first starts. If the file is invalid, close Minecraft and remove or rename it; the mod will recreate it with default settings on the next launch.
