<div align=center>
    <a href="https://thenounproject.com/browse/icons/term/origami-crane/"><img title="Origami Crane by Kelig Le Luron from The Noun Project" src="https://github.com/iCrazyBlaze/CustomItemsPlugin/blob/master/origami-logo.png?raw=true" align="center" style="max-width: 600px"></a>
  <a rel="license" href="http://creativecommons.org/licenses/by-nc/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by-nc/4.0/88x31.png" /></a><br />This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-nc/4.0/">Creative Commons Attribution-NonCommercial 4.0 International License</a>.
    <br />
    <p><h2>Custom blocks and items for <a href="https://papermc.io">Paper</a></h2></p>
  <br />
</div>

Origami is a Minecraft server plugin which allows a server admin to easily add configurable custom
blocks and
items using YAML configuration files, making use of
resource packs and custom model data to allow **vanilla** clients to connect and play with your custom content.

# Block Database

The positions and UUIDs of custom blocks are stored in one file per world, under the `_data` folder. **DO NOT** delete
files
in this folder or custom blocks placed in your world will not function correctly!
> ***NOTE:*** *when renaming worlds, be sure to rename the corresponding file.*

# Creating Custom Blocks

Custom block definitions go into `plugins/Origami/blocks`
If this directory is empty, then Origami will generate the following example definition, `rainbow_block.yml`:

```yml
block:
  ==: CustomBlock
  baseBlock: GLASS
  rightClickCommands:
    - tellraw @s {"text":"The block reverberates majestically.","italic":true,"color":"gray"}
  lore:
    - <rainbow>It shimmers beautifully in the sunlight.</rainbow>
  displayName: '&cR&6a&ei&an&9b&bo&5w &6B&el&ao&9c&bk'
  blockModelData: 3
  blockItemModelData: 4
  timeToBreak: 40
  dropExperience: 0
  drops:
    - DIAMOND(1)
  placeSound: BLOCK_AMETHYST_BLOCK_PLACE
  glowing: true
  canBePushed: true
  canBeMinedWith:
    - pickaxes
  isAffectedByFortune: true
  breakSound: BLOCK_AMETHYST_BLOCK_BREAK
  toolLevelRequired: 2
```

- **([Material](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html))** `baseBlock` - determines what
  block
  goes beneath the custom model displayed via an item frame. Only
  transparent blocks
  work when the item frame is not a glowing item frame (`glowing: false`). `GLASS` typically works the best, but
  using `SPAWNER` works too.


- **(Boolean)** `glowing` - determines if the item frame with custom model data (custom block item) placed by the player
  should be
  replaced with a glowing item frame once it has been spawned in. When a block uses a glowing item frame, it appears
  without lighting applied, so it is recommended that your `baseBlock` is a block that emits light when `glowing: true`.


- **(Integer)** `dropExperience` is how much XP should be dropped when the block is mined. XP can only be gained from a
  block if using the correct tool.


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


- **(List of special Material strings)** `drops` - a material or the ID of a custom item/block, optionally followed by a
  count in
  brackets. `DIAMOND` is one diamond, `origami:rainbow_block(3)` is 3 "rainbow blocks" (the custom block shown above,
  in item form.) If this list is `null`, the block will drop itself.


- **(Boolean)** `canBePushed` - if true, the block can be moved by Pistons and Sticky Pistons.

## Model data

> ℹ️ an example resource pack will be provided, and resource pack generation may be worked on in the future

- **(Integer)** `blockModelData` - the model data number for when the full block is rendered via an item frame.
- **(Integer)** `blockItemModelData` - the model data number for the item version of the block. When the player places
  this model in an item frame manually, it is the size of any other normal block.

> In this example we set `blockModelData` to `3`, and `blockItemModelData` to `4`.

