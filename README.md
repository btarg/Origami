<div align="center">
    <a href="https://thenounproject.com/browse/icons/term/origami-crane/"><img title="Origami Crane by Kelig Le Luron from The Noun Project" src="https://github.com/iCrazyBlaze/CustomItemsPlugin/blob/master/origami-logo.png?raw=true" align="center" style="max-width: 500px" alt="Origami"></a>
  <a rel="license" href="http://creativecommons.org/licenses/by-nc/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by-nc/4.0/88x31.png" /></a><br />This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-nc/4.0/">Creative Commons Attribution-NonCommercial 4.0 International License</a>.
    <br />
    <h2>Custom blocks and items for <a href="https://papermc.io">Paper</a></h2>
</div>

Origami is a Minecraft server plugin which allows a server admin to easily add configurable custom
blocks,
items and crafting recipes using YAML configuration files.

The plugin generates and hosts resource packs to allow **vanilla** clients to connect and play with your custom content.

[![Build status](https://github.com/btarg/Origami/actions/workflows/gradle.yml/badge.svg)](https://github.com/btarg/Origami/actions)

# Getting Started

> 📖 View the official docs on [GitBook](https://btarg.gitbook.io/origami-docs/) for information on how to use the
> plugin.

## Supported versions

This plugin will only ever support the latest version of Minecraft (Paper) at the time of its release.

## Reporting issues

You can report issues with the plugin or the docs on this GitHub repository's issues page.
When reporting a bug, send a full log file and walk
through the steps needed to replicate the bug.

# Current progress

> This is not a comprehensive list of features, but serves instead as a developer to-do list.

- [x] Reload commands for all content
- [x] "Content Packs": content can be organised into folders under the `custom` folder
- [x] Example content generation for first-time users
    - [x] Example crafting recipe
    - [x] Example block
    - [ ] Example item
    - [ ] Example models
- [x] Custom blocks via YAML
    - [x] Custom models via Minecraft JSON model format
    - [x] Custom block break speed (needs more work)
    - [x] Loot tables
    - [x] Sounds for breaking and placing blocks
    - [x] Blocks can be pushable
    - [ ] Custom stairs and slabs
    - [ ] Custom breaking particles
    - [ ] World awareness (e.g. detecting when another block is near it, when a player steps on it)
    - [ ] Custom containers
- [x] Custom items via YAML
    - [x] Custom models via Minecraft JSON model format
    - [x] Custom name, lore and flags
        - [x] Translated strings support
        - [x] Legacy formatting *and* [MiniMessage](https://docs.advntr.dev/minimessage/index.html) support
        - [ ] [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) support
    - [x] Custom durability (tools, armour etc.)
    - [x] Editable enchantments
        - [ ] Configurable allowed enchantments
    - [ ] Editable attributes (e.g. attack power, armour defense stat)
    - [ ] NBT override via NBT string
    - [ ] Custom food items (function on consume)
    - [ ] Custom tools and tool levels (may be impossible)
    - [ ] Custom sounds for breaking
- [x] Custom crafting recipes via YAML
    - [x] Cooking recipes (Furnaces, Campfires etc.)
    - [x] Smithing recipes (still very janky)
    - [ ] Potion recipes
    - [ ] Villager trades
    - [ ] Custom item repair recipes (defined in the item's YAML rather than as a separate "recipe")
- [ ] Custom enchantments
- [ ] YAML event system: events can be subscribed to such as when a block is broken, when an item is used or consumed
  etc. via a string in a YAML file
- [x] Resource pack generation
    - [x] Resource pack hosting
    - [ ] Resource pack merging / multiple pack support
- [ ] Web UI for visual editing *(see preliminary work [here](https://github.com/btarg/vuejava))*
    - [ ] Backend API with Javalin
    - [ ] Vue.js frontend

# Credits and special thanks

- The [PaperMC Discord](https://discord.gg/papermc) for being incredibly helpful and quick to respond to any questions
- [Unnamed Team](https://unnamed.team) for creating
  the [Creative](https://unnamed.team/docs/creative/latest/getting-started) API and [yusshu](https://github.com/yusshu)
  for helping me with implementing it
- [Dannegm on GitHub](https://github.com/dannegm/BlockEntities) for providing an initial implementation of custom blocks
  using Item Display entities