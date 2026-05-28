# CloudRegen
<img width="1536" height="1024" alt="image" src="https://github.com/user-attachments/assets/9c188bfb-0abf-4cfe-9f20-397d14f51227" />

[center]
[size=6][b][color=#00B4D8]☁ CloudRegen[/color][/b][/size]
[size=4][color=#90E0EF]High-Performance Region Regeneration for Modern Servers[/color][/size]
[i]Built for Paper • Folia-ready • Designed for scale[/i]
[/center]

[hr]

[b][size=5][color=#48CAE4]📋 Overview[/color][/size][/b]

CloudRegen is a next-generation region regeneration plugin that lets you save, manage, and automatically restore areas of your world — without lag spikes, without bloated file sizes, and without fighting your server software.

Whether you run PvP arenas, mob farms, event zones, minigame maps, or protected build areas, CloudRegen gives staff a [b]fast, visual, professional workflow[/b] to keep regions clean while players keep playing.

[hr]

[b][size=5][color=#48CAE4]⚡ Why CloudRegen?[/color][/size][/b]

[list]
[*][b]Performance-first architecture[/b] — Chunk-batched regeneration, adaptive block budgets, and async scheduling keep TPS stable even on large regions.
[*][b]Compact binary snapshots[/b] — Regions are stored in compressed binary format, not massive schematic folders or slow YAML block dumps.
[*][b]Folia supported[/b] — Native region-thread scheduling on Folia; classic Paper scheduler on non-Folia servers.
[*][b]Smart regeneration modes[/b] — Full restore, placed-block tracking, or broken/modified-only repair — you choose per region.
[*][b]Premium in-game editor[/b] — No memorizing config keys; manage everything from polished GUIs with sounds, titles, and action bars.
[*][b]26 languages built-in[/b] — English, Spanish, German, French, Portuguese, Polish, Russian, Chinese, Japanese, Korean, Arabic, and more.
[*][b]Tiny footprint[/b] — Lightweight shaded JAR; no unnecessary dependencies bundled into your server.
[*][b]PlaceholderAPI ready[/b] — Show live countdowns on scoreboards, tab, or chat with [color=#00B4D8]%cr_time_<region>%[/color].
[/list]

[quote][i]Most regen plugins either restore everything (wasting CPU) or break on large areas. CloudRegen was built from the ground up to do [b]selective, scheduled, safe[/b] restoration at server scale.[/i][/quote]

[hr]

[b][size=5][color=#48CAE4]🛠 Core Features[/color][/size][/b]

[b]🪄 Selection Wand[/b]
[list]
[*] Left-click a [b]block[/b] to set Position 1
[*] Right-click a [b]block[/b] to set Position 2
[*] Air clicks are ignored — only real blocks count
[*] Customizable wand material & name in config
[/list]

[b]💾 Region Snapshots[/b]
[list]
[*] Save any cuboid selection as a named region
[*] Redefine regions in-place without deleting data
[*] Compressed binary storage with configurable compression level
[*] Per-region metadata (interval, mode, messages, safety, stats)
[/list]

[b]🔁 Regeneration Modes[/b]
[table]
[tr]
[td][b]FULL[/b][/td]
[td]Restores the entire saved snapshot on each cycle — perfect for arenas & farms.[/td]
[/tr]
[tr]
[td][b]PLACED[/b][/td]
[td]Only restores blocks players have placed inside the region.[/td]
[/tr]
[tr]
[td][b]BROKEN[/b][/td]
[td]Only restores blocks that were broken or changed from the snapshot — ideal for mines & grief-prone zones.[/td]
[/tr]
[/table]

[b]🛡 Player Safety[/b]
[list]
[*] Optional safety teleport during regen (moves players out of falling blocks)
[*] Per-chunk safety checks for large regions
[*] Toggle per region in the editor
[/list]

[b]📢 Messaging[/b]
[list]
[*] Custom regen announcement per region
[*] Broadcast to server or only players inside the region
[*] Titles, subtitles, action bars, and sounds
[/list]

[b]🖥 Premium GUI System[/b]
[list]
[*][b]Region Browser[/b] — Paginated list of all regions; click to edit
[*][b]Region Editor[/b] — Edit display name, interval, mode, message, safety, audience, teleport to region, save & delete
[*][b]Delete Confirmation[/b] — Prevents accidental region removal
[*][b]Chat Input[/b] — Edit values in-chat with cancel support
[/list]

[b]📊 Staff Tools[/b]
[list]
[*] Live debug panel (active regens, queue size, blocks/sec, cycle budget)
[*] Per-region statistics (total regens, blocks applied)
[*] Force-regen command for instant testing
[/list]

[hr]

[b][size=5][color=#48CAE4]⌨ Commands[/color][/size][/b]

[code]
/cr help              — Command menu
/cr wand              — Get the selection wand
/cr save [name]       — Save your current selection
/cr redefine <name>   — Redefine an existing region's bounds
/cr editor            — Open the region browser GUI
/cr edit <name>       — Open a region's editor directly
/cr force <name>      — Force regeneration now
/cr list              — List all saved regions
/cr delete <name>     — Delete a region
/cr reload            — Reload config & language files
/cr debug             — View runtime performance stats
[/code]

[b]Permissions[/b]
[code]
cloudregen.use    — Basic command access (default: op)
cloudregen.admin  — Administrative access (default: op)
[/code]

[hr]

[b][size=5][color=#48CAE4]🔗 PlaceholderAPI[/color][/size][/b]

[color=#00B4D8][b]%cr_time_<regionname>%[/b][/color] — Seconds until the next automatic regeneration.

[i]Example:[/i] [color=#00B4D8]%cr_time_arena%[/color] → [b]142[/b]

Perfect for scoreboards, holograms, TAB, and custom UIs.

[hr]

[b][size=5][color=#48CAE4]🌍 Languages[/color][/size][/b]

Set [b]language: en[/b] (or any code) in [b]config.yml[/b]. Bundled translations:

[color=#90E0EF]en • es • ru • de • fr • pt • pl • tr • zh • id • it • vi • nl • ko • cs • th • hu • ar • ja • uk • sv • da • ro • sk • he • lt[/color]

All messages are editable in [b]plugins/CloudRegen/lang/[/b].

[hr]

[b][size=5][color=#48CAE4]⚙ Configuration Highlights[/color][/size][/b]

[list]
[*] Blocks per cycle & max chunks per cycle
[*] Adaptive regeneration budget (auto-scales under load)
[*] Compression level & write buffer for snapshots
[*] Default safety teleport toggle
[*] Debug mode for performance monitoring
[/list]

[hr]

[b][size=5][color=#48CAE4]✅ Requirements[/color][/size][/b]

[list]
[*][b]Minecraft:[/b] 1.21+
[*][b]Server:[/b] Paper, Purpur, or Folia
[*][b]Java:[/b] 21
[*][b]Optional:[/b] PlaceholderAPI
[/list]

[hr]

[center]
[size=4][b][color=#00B4D8]☁ CloudRegen[/color][/b][/size]
[i]Clean regions. Stable TPS. Zero hassle.[/i]

[b]CloudStudios[/b] — Built for servers that take performance seriously.
[/center]
