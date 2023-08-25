<a href="https://thenounproject.com/browse/icons/term/origami-crane/"><img title="Origami Crane by Kelig Le Luron from The Noun Project" src="https://github.com/iCrazyBlaze/CustomItemsPlugin/blob/master/origami-logo.png?raw=true" align="center" style="max-width: 600px"></a>

A Minecraft server plugin for [Paper](https://papermc.io) which allows a server admin to easily add configurable custom
blocks and
items using YAML configuration files, making use of
resource packs and custom model data to allow vanilla clients to connect.

# Block Database

The positions and IDs of custom blocks are stored one file per world, under the `_data` folder. **DO NOT** delete files
in this folder or custom blocks placed in your world will not function correctly!
> ***NOTE:*** *when renaming worlds, be sure to rename the corresponding file.*

# Creating Custom Blocks

Custom block definitions go into `plugins/Origami/blocks`
If this directory is empty, then Origami will generate the following example definition, `rainbow_block.yml`:

```yml
block:
  ==: io.github.btarg.definitions.CustomBlockDefinition
  baseBlock: GLASS
  glowing: false
  dropExperience: 0
  lore:
    - <rainbow>It shimmers beautifully in the sunlight.</rainbow>
  rightClickCommands:
    - tellraw @s {"text":"The block reverberates magestically.","italic":true,"color":"gray"}
  canBeMinedWith:
    - pickaxes
  displayName: §cR§6a§ei§an§9b§bo§5w §6B§el§ao§9c§bk
  dropBlock: true
  blockModelData: 4
  toolLevelRequired: 2
  blockItemModelData: 3
  timeToBreak: 40
  breakSound: "BLOCK_AMETHYST_BLOCK_BREAK"
  placeSound: "BLOCK_AMETHYST_BLOCK_PLACE"
```

- **([Material](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html))** `baseBlock` - determines what
  block
  goes beneath the custom model displayed via an item frame. Only
  transparent blocks
  work when the item frame is not a glowing item frame (`glowing: false`)


- **(Boolean)** `glowing` - determines if the item frame with custom model data (custom block item) placed by the player
  should be
  replaced with a glowing item frame once it has been spawned in.


- **(Integer)** `dropExperience` is how much XP should be dropped when the block is mined. XP can only be gained from a
  block if using the correct tool


- **(List of formatted strings)** `lore` - lore to display on the custom item. Can use either legacy formatting
  codes (`§`
  or `&`,
  but *not both at once*) or [MiniMessage.](https://docs.advntr.dev/minimessage/format.html)


- **(List of Minecraft commands)** `rightClickCommands` - if not null or empty, then these commands will be run when the
  player right-clicks the block. They are wrapped by `execute as <player name>`, meaning that the `@s` selector refers
  to the player who clicked the block. Runs twice if there is an item in the player's offhand.


- **(List of tools)** `canBeMinedWith` - multiple "preferred tools" can be set here, with valid tool types
  being: `pickaxes`, `axes`, `shovels`, `hoes`, and `swords`. A block can only be broken quickly or drop its items if
  one of the correct tools is used. Usually blocks in Minecraft only set one of these.


- **(Formatted string)** `displayName` - like with lore, this can use MiniMessage or legacy format codes. You can also
  use
  a language string, and set the item's name in your resource pack's lang file instead - formatting included.


- **(Boolean)** `dropBlock` - set this to `true` if the block should always drop one of itself when broken with the
  right tool, as if Silk Touch is always being used. Otherwise, you can use a custom loot definition

## Model data

> ℹ️ an example resource pack will be provided, and resource pack generation may be worked on in the future

- **(Integer)** `blockModelData` - the model data number for when the full block is rendered via an item frame.
- **(Integer)** `blockItemModelData` - the model data number for the item version of the block. When the player places
  this model in an item frame manually, it is the size of any other normal block.

> In this example we set `blockModelData` to `4`, and `blockItemModelData` to `3`.

