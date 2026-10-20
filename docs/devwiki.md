# DevWiki

Developer wiki for building and maintaining Armor Visibility.

## Project layout

Each loader and Minecraft version has its own Gradle project:

| Folder | Target |
| --- | --- |
| `ArmorVisibility-Fabric/` | Fabric, Minecraft 1.21.11 |
| `ArmorVisibility-Fabric-26.1-26.3/` | Fabric, Minecraft 26.3 |
| `ArmorVisibility-Quilt/` | Quilt, Minecraft 1.21.11 |
| `ArmorVisibility-Quilt-26.1-26.3/` | Quilt, Minecraft 26.3 |
| `ArmorVisibility-Neoforge/` | NeoForge, Minecraft 1.21.11 |

Each folder contains its own Gradle wrapper and sources. The client configuration screen and visibility rules are implemented in the corresponding project's `src/main/java` tree.

## Build

Use Java 21 or newer. In PowerShell, change into the project folder you want to build and run:

```powershell
.\gradlew.bat build
```

For example:

```powershell
Set-Location .\ArmorVisibility-Fabric
.\gradlew.bat build
```

The generated mod JAR is placed in that project's `build\libs\` directory. Build each loader/version project separately; do not copy a JAR to a different loader or Minecraft version.

## Configuration behavior

The client settings are stored in `config/armorvisibility.json`. The settings screen writes each change immediately. Rendering behavior is implemented by the armor, cape, and elytra mixins. Keep the loader-specific sources aligned when changing shared behavior.
