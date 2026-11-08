# Installation

## Choose the right build

Use a mod file made for your exact Minecraft version and mod loader. The current project declares these Minecraft version ranges:

| Loader | Minecraft versions represented in the project |
| --- | --- |
| Fabric | 1.21.1–1.21.11 and 26.1–26.3 |
| Quilt | 1.21.1–1.21.11 and 26.1–26.3 |
| NeoForge | 1.21.1–1.21.11 and 26.1–26.3 |
| Forge | 1.21.1, 1.21.3–1.21.11, and 26.1–26.3 (Forge has no 1.21.2 release) |

The version-specific project folders are not interchangeable. Download a release from [Modrinth](https://modrinth.com/project/armorvisibility) or [CurseForge](https://www.curseforge.com/minecraft/mc-mods/armorvisibility) and check its game-version and loader labels before installing it. NeoForge 1.21.1–1.21.10 have separate projects adapted to their rendering APIs alongside the existing 1.21.11 and 26.x projects.

## Requirements

- Minecraft version supported by the selected release
- The matching Fabric, Quilt, NeoForge, or Forge loader
- Java 21 for Minecraft 1.21.x; Java 25 for Minecraft 26.x, following the Minecraft launcher requirements

## Install

1. Install the loader for the Minecraft version you plan to play.
2. Download the matching Armor Visibility release.
3. Put the mod file in that Minecraft installation's `mods` folder.
4. Start the game with the matching loader profile.

Do not put multiple Armor Visibility builds for different loaders or game versions in the same `mods` folder. Mod Menu is optional for Fabric and Quilt; it adds a configuration entry to its mod list. The pause menu's Armor Visibility icon next to Statistics opens the settings on every loader.
