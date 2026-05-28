# CloudRegen
<img width="1536" height="1024" alt="image" src="https://github.com/user-attachments/assets/9c188bfb-0abf-4cfe-9f20-397d14f51227" />

## ☁ CloudRegen

**High-Performance Region Regeneration for Modern Servers**  
*Paper 1.21+ • Folia Supported • Free*

---

## 📋 Overview

CloudRegen is a high-performance region regeneration plugin built for modern Minecraft servers.  
Save any region as a snapshot, choose exactly how it should regenerate, and manage everything through a premium in-game GUI workflow.

It is designed for arenas, mines, event maps, and farm resets where performance and stability matter.

---

## ⚡ Core Features

- **Performance-first regeneration** — Chunk-batched processing with adaptive budgets to reduce lag spikes.
- **Three regeneration modes** — `FULL`, `PLACED`, and `BROKEN`.
- **Binary snapshot storage** — Fast and compact region data format.
- **Folia support** — Works with Folia region scheduling and Paper servers.
- **Premium region editor GUI** — Manage interval, mode, messages, safety, and audience in-game.
- **Safety teleport system** — Prevents players being trapped during active regeneration.
- **26 built-in languages** — Easy localization via `plugins/CloudRegen/lang/`.
- **PlaceholderAPI integration** — `%cr_time_<region>%` countdown support.

---

## 🔁 Regeneration Modes

| Mode | Behavior |
|---|---|
| `FULL` | Restores the entire saved region snapshot. |
| `PLACED` | Restores only blocks tracked as placed by players. |
| `BROKEN` | Restores snapshot blocks that were broken/modified from their saved state. |

---

## ⌨ Commands

```txt
/cr help              - Show help menu
/cr wand              - Get selection wand
/cr save [name]       - Save selected region
/cr redefine <name>   - Redefine existing region
/cr editor            - Open region browser GUI
/cr edit <name>       - Open editor for region
/cr force <name>      - Force regeneration now
/cr list              - List saved regions
/cr delete <name>     - Delete region
/cr reload            - Reload config/lang
/cr debug             - Show runtime stats


### Permissions
```txt
cloudregen.use
cloudregen.admin
