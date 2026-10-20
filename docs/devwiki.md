# DevWiki

This page explains how the Armor Visibility source code is organized and how its main parts work. It is a code-reading guide, not a build or redistribution guide.

The project is marked **ARR (All Rights Reserved)**. Publicly readable source code does not grant permission to copy, modify, build, or redistribute the mod. See the repository's [LICENSE](https://github.com/nexi449/Armor-Visibility/blob/main/LICENSE).

## How the code is organized

The loader-specific projects contain corresponding implementations for their supported Minecraft and loader versions. The Fabric 1.21.11 source links below illustrate the main flow; names and rendering APIs can differ between versions and loaders.

| Code area | Responsibility |
| --- | --- |
| [`ArmorVisibilityClient`](https://github.com/nexi449/Armor-Visibility/blob/main/ArmorVisibility-Fabric/src/main/java/com/example/armorvisibility/ArmorVisibilityClient.java) | Client startup and the pause-menu configuration entry |
| [`ArmorVisibilityConfig`](https://github.com/nexi449/Armor-Visibility/blob/main/ArmorVisibility-Fabric/src/main/java/com/example/armorvisibility/ArmorVisibilityConfig.java) | Settings, defaults, getters/setters, and JSON persistence |
| [`ArmorVisibilityConfigScreen`](https://github.com/nexi449/Armor-Visibility/blob/main/ArmorVisibility-Fabric/src/main/java/com/example/armorvisibility/compat/ArmorVisibilityConfigScreen.java) | In-game toggle buttons and their labels |
| [`ArmorVisibilityModMenuApi`](https://github.com/nexi449/Armor-Visibility/blob/main/ArmorVisibility-Fabric/src/main/java/com/example/armorvisibility/compat/ArmorVisibilityModMenuApi.java) | Connects the settings screen to Mod Menu |
| [`ArmorFeatureRendererMixin`](https://github.com/nexi449/Armor-Visibility/blob/main/ArmorVisibility-Fabric/src/main/java/com/example/armorvisibility/mixin/ArmorFeatureRendererMixin.java) | Decides whether an armor piece is rendered |
| [`CapeFeatureRendererMixin`](https://github.com/nexi449/Armor-Visibility/blob/main/ArmorVisibility-Fabric/src/main/java/com/example/armorvisibility/mixin/CapeFeatureRendererMixin.java) | Cancels cape rendering when the cape option is off |
| [`ElytraFeatureRendererMixin`](https://github.com/nexi449/Armor-Visibility/blob/main/ArmorVisibility-Fabric/src/main/java/com/example/armorvisibility/mixin/ElytraFeatureRendererMixin.java) | Cancels elytra rendering when the elytra option is off |
| [`PauseMenuIconMixin`](https://github.com/nexi449/Armor-Visibility/blob/main/ArmorVisibility-Fabric/src/main/java/com/example/armorvisibility/mixin/PauseMenuIconMixin.java) | Draws the mod icon in the pause menu |

## Runtime flow

1. `ArmorVisibilityClient.onInitializeClient()` loads the saved configuration.
2. A screen event detects the pause menu, finds its Statistics button, and adds the Armor Visibility button beside it.
3. Selecting that button opens `ArmorVisibilityConfigScreen`. If Mod Menu is installed, `ArmorVisibilityModMenuApi` opens the same screen from the mod list.
4. Each toggle calls a setter on `ArmorVisibilityConfig`. The setter updates the in-memory value and saves the JSON file.
5. During rendering, the armor, cape, and elytra mixins read the relevant setting. They cancel only the rendering call; they do not remove or change equipped items.

## Configuration model

`ArmorVisibilityConfig` holds one `Data` object. `INSTANCE` is the shared config used by the screen and render hooks.

- `armorVisible` is the global armor switch. When false, the armor mixin cancels armor rendering before checking an individual slot.
- `hideHelmet`, `hideChestplate`, `hideLeggings`, and `hideBoots` represent the per-slot hide choices. `isArmorPartHidden(EquipmentSlot)` maps Minecraft's equipment slots to these values.
- `hideForOtherPlayers` controls whether per-slot hide choices also apply to other player entities. It does not override the global armor switch.
- `playerOnly` limits the armor mixin's rules to entities whose render state is a player.
- `keepCapeVisible` and `keepElytraVisible` independently control whether those render calls are cancelled.

The JSON file is `config/armorvisibility.json`. Defaults are created when the file is missing. If JSON cannot be read, the config logs an error and uses defaults for that run. Each setter saves immediately.

## Rendering decisions

### Armor

`ArmorFeatureRendererMixin` injects at the start of the armor render method and can cancel it. The checks run in this order:

1. A missing equipment slot is ignored.
2. If **Player Only** is enabled and the rendered entity is not a player, the mod leaves that armor render unchanged.
3. If **Global Armor** is off, armor rendering is cancelled.
4. Otherwise, the mixin checks the slot-specific hide setting. For a different player, that setting is applied only when **Other Armor** is enabled.

The global switch therefore hides armor regardless of the individual slot or other-player option.

### Cape and elytra

The cape and elytra mixins use separate render hooks. Each cancels its render call when its own **Cape** or **Elytra** setting is off. These checks are independent of the armor-slot and **Player Only** settings.

## Settings screen

`ArmorVisibilityConfigScreen` builds the buttons in `init()`. Slot buttons show **Shown** or **Hidden**; the other toggles show **On** or **Off**, except **Other Armor**, which shows **Hidden** or **Visible**. After a click, the screen is rebuilt so the label reflects the saved value. **Done** returns to the parent screen.

## Reading changes safely

When inspecting a change, trace a setting from its screen button to its config setter and then to the render hook that reads it. Compare the corresponding loader/version implementation before drawing conclusions: Minecraft rendering method names and signatures are version-specific.
