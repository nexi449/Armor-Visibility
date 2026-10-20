# Configuration

Open the configuration from the Armor Visibility icon beside **Statistics** in the pause menu, or from the mod's configuration entry in Mod Menu. Mod Menu is optional.

Every toggle saves its value immediately and updates its label. **Done** closes the screen and returns to the previous menu.

## Armor controls

| Button | What it does |
| --- | --- |
| **Helmet** | Shows or hides the helmet render layer. |
| **Chestplate** | Shows or hides the chestplate render layer. |
| **Leggings** | Shows or hides the leggings render layer. |
| **Boots** | Shows or hides the boots render layer. |
| **Global Armor** | Turns armor rendering on or off globally. When off, armor is hidden regardless of the individual slot settings. |

The slot buttons display **Shown** or **Hidden**. When Global Armor is off, armor is hidden even if a slot button says **Shown**.

## Other visibility controls

| Button | What it does |
| --- | --- |
| **Cape** | **On** keeps capes visible; **Off** hides them. |
| **Elytra** | **On** keeps elytra visible; **Off** hides them. |
| **Player Only** | **On** limits armor visibility rules to player entities. **Off** lets those rules apply to other entity types too. |
| **Other Armor** | **Hidden** applies per-slot hide rules to other players; **Visible** leaves other players' armor visible under those rules. |
| **Done** | Closes the settings screen and returns to the previous menu. |

Cape and elytra visibility are independent of the armor-slot buttons.

## Config file

Settings are stored in `config/armorvisibility.json` in the Minecraft game directory. The file is created automatically. You can edit it while the game is closed; changes made in the settings screen are written to the file as you click the buttons.
