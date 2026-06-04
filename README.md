# Don't Eat That

A Minecraft mod that adds a comprehensive nutrient system with 7 nutrient types, each providing distinct status effects and gameplay mechanics. Eat a balanced diet to gain benefits, or overindulge in cactus to unlock a dangerous thorns-like body curse.

## Features

- **7 Nutrient Types**: Vitamin A, B6, C, E, K, Fiber, and Spiky. Each food item contributes to one or more nutrient pools.
- **Status Effects**: Maintain nutrient levels above 1.5 to activate lasting effects:
  - Vitamin A: Night Vision
  - Vitamin B6: Luck
  - Vitamin C: Poison immunity, passive regeneration (stronger at higher levels)
  - Vitamin E: Damage reduction on hit
  - Vitamin K: Regeneration boost
  - Fiber: Faster nutrient absorption
  - Spiky: Thorns body -- reflects damage back to attackers, but amplifies damage from falls, fire, drowning, suffocation, starvation, and poison
- **Visual HUD**: On-screen nutrient bar shows your current nutrient point icons. Press H to toggle, K to open the nutrition guide.
- **New Items**: 9 fruit types found in leaves, vines, and grass. Fiber items and a seed block with crafting and smelting recipes.
- **Customizable**: Toggle effects and fiber mechanics, adjust decay rate, and configure HUD position via the config file.

## Nutrient Guide

| Nutrient | Source Foods | Effect |
|----------|-------------|--------|
| Vitamin A | Carrots, golden carrots, melon, pumpkin pie, mango | Night Vision at 1.5+ |
| Vitamin B6 | Potatoes, baked potatoes, banana, beetroot, rabbit stew | Luck + Combat Dodge at 1.5+ |
| Vitamin C | Apples, oranges, sweet berries, glow berries, assorted berries | Poison cure + Regeneration at 1.5+ |
| Vitamin E | Sunflower, seeds, beetroot soup | Damage reduction on hit at 1.5+ |
| Vitamin K | Dandelion, kelp, dried kelp, sea pickle, allium | Regeneration at 1.5+ |
| Fiber | Bread, cookie, cake, honey, fiber items | Faster absorption at 1.5+ |
| Spiky | Cactus | Thorns reflection at 5+ points, but amplifies non-mob damage |

## Spiky System

Eating cactus grants spiky points. At 5 or more spiky icons:
- **Thorns**: Damage dealt to you is reflected back to the attacker, multiplied by your spiky level (6 icons = 1.2x, 7 = 1.4x, and so on)
- **Curse**: Fall damage, fire, lava, drowning, suffocation, starvation, and poison damage are amplified by the same multiplier plus a flat bonus equal to your spiky point count

## Configuration

Edit `config/dont-eat-that.json` after running the mod once:

- `hudEnabled`: Show/hide the nutrient HUD (default: true)
- `hudShowOnSneak`: Only show HUD while sneaking (default: false)
- `hudAnchor`: HUD position (0: top-left, 1: top-right, 2: bottom-left, 3: bottom-right)
- `decayMultiplier`: Global nutrient decay rate (default: 1.0)
- `enableEffects`: Toggle all nutrient-based status effects (default: true)
- `enableFiber`: Toggle the fiber absorption system (default: true)

## Requirements

- Minecraft 1.20.4
- Fabric Loader 0.19.2 or later
- Fabric API 0.97.3 or later

## License

This project is available under the CC0 1.0 Universal license.
