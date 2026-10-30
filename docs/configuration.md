# Configuration

Open the configuration from the Armor Visibility icon beside **Statistics** in the pause menu, or from the mod's configuration entry in Mod Menu (Fabric/Quilt). NeoForge exposes the same screen through its mod configuration entry. Mod Menu is optional.

The screen combines the controls with a live player preview. Changes are saved automatically; **Done** returns to the previous menu.

## Armor and rendering

| Control | What it does |
| --- | --- |
| **Helmet**, **Chestplate**, **Leggings**, **Boots** | Adjust opacity from fully transparent to fully visible. The adjacent **Glint** button independently enables or disables enchantment glint for that armor slot. |
| **Global Armor** | Turns armor rendering on or off globally, regardless of slot opacity. |
| **Offhand** | Adjusts opacity for the offhand item in first- and third-person rendering. |

The preview displays your player model while configuring the screen. Some specialized item renderers may not use the same transparency path as ordinary rendered items.

## Other visibility controls

| Control | What it does |
| --- | --- |
| **Cape** | Keeps capes visible when on; hides them when off. |
| **Elytra** | Keeps elytra visible when on; hides them when off. |
| **Player Only** | Limits armor visibility rules to player entities. |
| **Apply to Others** | Applies per-slot armor visibility rules to other players as well. |

Cape and elytra visibility are independent of the armor-slot controls.

## Presets

Five preset cards store the current armor opacity and glint settings, offhand opacity, and visibility toggles. **Left-click** an empty card to save the current settings; left-click a saved card to load it. **Right-click** a saved card to delete it. Hover over a card for a summary of its settings.

## Config file

Settings and presets are stored in `config/armorvisibility.json` in the Minecraft game directory. The file is created automatically. Changes made in the settings screen are written as you interact with the controls.
