# EconomyShopGUI

A comprehensive Minecraft Paper plugin that provides a complete economy shop system with GUI-based buying and selling mechanics.

## Description

EconomyShopGUI is a feature-rich economy plugin for Minecraft Paper servers that integrates with Vault for economy management. It provides players with an intuitive GUI-based shop system where they can buy and sell items, with support for multiple shop sections, multi-page inventories, and flexible pricing configurations.

## Features

- ✅ **GUI-Based Shop System** - Intuitive inventory-based shopping interface
- ✅ **Buy System** - Purchase items with quantity adjustment (+1, +16, +32, +64)
- ✅ **Sell System** - Sell items directly or via GUI interface
- ✅ **Multi-Section Shops** - Organize items into different shop sections
- ✅ **Multi-Page Support** - Handle unlimited items across multiple pages
- ✅ **Vault Integration** - Full economy support via Vault API
- ✅ **YAML Configuration** - Easy-to-manage configuration files
- ✅ **Admin Commands** - Complete shop management tools
- ✅ **Transaction Logging** - All transactions logged to console

## Requirements

- **Minecraft Version:** 1.21+
- **Server Software:** Paper/Spigot
- **Dependencies:** Vault (for economy support)
- **Java Version:** 21+

## Installation

1. Download the latest `test.jar` from releases
2. Place the JAR file in your server's `plugins` folder
3. Install Vault plugin if not already installed
4. Install an economy plugin (e.g., EssentialsX)
5. Restart your server
6. Configuration files will be created in `plugins/EcoGUI/`

## Commands

### Player Commands

#### `/shop`
Opens the main shop GUI where players can browse and purchase items.

**Usage:** `/shop`

**Permissions:** None (all players)

**Example:**
```
/shop
```

---

#### `/sell <item name> <amount>`
Sell a specific item by name with a specified quantity.

**Usage:** `/sell <item name> <amount>`

**Arguments:**
- `<item name>` - The material name (e.g., DIAMOND, STONE, IRON_ORE)
- `<amount>` - Number of items to sell (must be > 0)

**Permissions:** None (all players)

**Examples:**
```
/sell DIAMOND 10
/sell STONE 64
/sell IRON_ORE 32
```

---

#### `/sell hand [amount]`
Sell the item currently held in your main hand.

**Usage:** `/sell hand [amount]`

**Arguments:**
- `[amount]` - Optional. Number of items to sell. If omitted, sells all items of that type.

**Permissions:** None (all players)

**Examples:**
```
/sell hand 20          # Sell 20 of the held item
/sell hand             # Sell all of the held item type
```

---

#### `/sellgui`
Opens the sell GUI where you can place items and sell them all at once.

**Usage:** `/sellgui`

**Permissions:** None (all players)

**Features:**
- 45 empty slots to place items
- Glass panels on the last row
- Close button in the center to sell all items
- Cannot drop items from this GUI

**Example:**
```
/sellgui
```

---

### Admin Commands

#### `/shop`
Opens the main shop GUI (same as player command).

**Usage:** `/shop`

---

#### `/rshop`
Reloads all shop and section configurations from YAML files.

**Usage:** `/rshop`

**Permissions:** `ecogui.admin` or OP

**Example:**
```
/rshop
```

---

#### `/csection <displayname> <slot> <icon>`
Creates a new shop section.

**Usage:** `/csection <displayname> <slot> <icon>`

**Arguments:**
- `<displayname>` - Display name for the section (e.g., "Ores", "Blocks")
- `<slot>` - Inventory slot (0-53, excluding 45 and 53)
- `<icon>` - Material to use as the section icon (e.g., DIAMOND_ORE)

**Permissions:** `ecogui.admin` or OP

**Examples:**
```
/csection Ores 0 DIAMOND_ORE
/csection Blocks 1 STONE
/csection Rare 2 EMERALD_ORE
```

---

#### `/aitem <item name> <section name> <buy price> <sell price>`
Add an item to a shop section by material name.

**Usage:** `/aitem <item name> <section name> <buy price> <sell price>`

**Arguments:**
- `<item name>` - Material name (e.g., DIAMOND, STONE)
- `<section name>` - Name of the section to add to
- `<buy price>` - Price players pay to buy (must be > 0)
- `<sell price>` - Price players receive when selling (use -1.0 for non-sellable)

**Permissions:** `ecogui.admin` or OP

**Examples:**
```
/aitem DIAMOND Ores 100 50
/aitem STONE Blocks 10 5
/aitem EMERALD Rare 500 250
/aitem BEDROCK Rare 9999 -1.0
```

---

#### `/hitem <section name> <buy price> <sell price>`
Add the item currently held in your main hand to a shop section.

**Usage:** `/hitem <section name> <buy price> <sell price>`

**Arguments:**
- `<section name>` - Name of the section to add to
- `<buy price>` - Price players pay to buy
- `<sell price>` - Price players receive when selling

**Permissions:** `ecogui.admin` or OP

**Examples:**
```
/hitem Ores 100 50
/hitem Blocks 10 5
```

---

## Configuration

### Directory Structure

```
plugins/EcoGUI/
├── sections/
│   ├── ores.yml
│   ├── blocks.yml
│   └── rare.yml
└── shops/
    ├── ores.yml
    ├── blocks.yml
    └── rare.yml
```

### Section Configuration Example

**File:** `plugins/EcoGUI/sections/ores.yml`

```yaml
enable: true
item:
  material: DIAMOND_ORE
  displayname: Ores
slot: 0
```

### Shop Configuration Example

**File:** `plugins/EcoGUI/shops/ores.yml`

```yaml
page1:
  items:
    0:
      material: DIAMOND
      buy: 100.0
      sell: 50.0
    1:
      material: IRON_ORE
      buy: 20.0
      sell: 10.0
    2:
      material: GOLD_ORE
      buy: 50.0
      sell: 25.0
    3:
      material: EMERALD
      buy: 500.0
      sell: 250.0
```

## Permissions

| Permission | Description |
|-----------|-------------|
| `ecogui.admin` | Access to admin commands (/csection, /aitem, /hitem, /rshop) |
| None | Players can use /shop, /sell, /sellgui |

## To-Do List

### Planned Features

- [ ] **Sell Price Lookup** - Dynamically read sell prices from shop configuration
- [ ] **Bulk Operations** - Sell multiple item types at once
- [ ] **Price History** - Track price changes over time
- [ ] **Shop Permissions** - Per-section access control
- [ ] **Custom Item Names** - Support for custom display names in shop
- [ ] **Item Lore** - Add lore/description to shop items
- [ ] **Sell GUI Improvements** - Add item preview and price calculation
- [ ] **Database Support** - Optional MySQL/SQLite for transaction history
- [ ] **Shop Notifications** - Broadcast shop updates to players
- [ ] **Price Multipliers** - Support for dynamic pricing based on supply/demand
- [ ] **Sell All Command** - `/sellall` to sell entire inventory
- [ ] **Shop Search** - Search functionality for items
- [ ] **Favorites** - Save favorite items for quick access
- [ ] **Price Limits** - Set min/max prices for items
- [ ] **Admin GUI** - GUI-based shop management
- [ ] **Localization** - Multi-language support

### Bug Fixes & Improvements

- [ ] Optimize inventory loading for large shops
- [ ] Add more detailed error messages
- [ ] Improve performance for shops with many items
- [ ] Add command tab completion
- [ ] Better handling of invalid configurations
- [ ] Add shop statistics and analytics

## Troubleshooting

### Items not appearing in shop
- Ensure the section is enabled in the section YAML file
- Check that the shop YAML file exists for the section
- Run `/rshop` to reload configurations

### Players can't sell items
- Verify the item is configured in the shop with a valid sell price
- Check that the sell price is not -1.0 (reserved for non-sellable items)
- Ensure Vault and an economy plugin are installed

### Sell GUI not working
- Make sure players can place items in the inventory
- Check that the close button is clickable
- Verify that items are being detected correctly

## Support

For issues, feature requests, or questions:
- Create an issue on GitHub
- Check the configuration files in `plugins/EcoGUI/`
- Review the console logs for error messages

## License

This project is provided as-is for use on Minecraft servers.

## Credits

Developed for Paper/Spigot servers with Vault economy integration.

---

**Version:** 1.0.0  
**Last Updated:** November 5, 2025
