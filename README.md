# BetterEnderchests

A virtual Ender Chest plugin for Paper and Folia servers, with bigger chests per rank, multiple pages, sharing, team chests and built-in backups.

[![Latest release](https://img.shields.io/github/v/release/theprodogy/BetterEnderchests?label=release)](https://github.com/theprodogy/BetterEnderchests/releases/latest)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11%20--%2026.3-brightgreen)](#compatibility)
[![Platform](https://img.shields.io/badge/platform-Paper%20%7C%20Folia-blue)](#compatibility)
[![Java](https://img.shields.io/badge/Java-21%2B-orange)](#compatibility)
[![License](https://img.shields.io/badge/license-All%20Rights%20Reserved-lightgrey)](#license)

## Quick start

1. Download `BetterEnderchests.jar` from the [latest release](https://github.com/theprodogy/BetterEnderchests/releases/latest).
2. Drop it into your server's `plugins/` folder.
3. Restart the server.
4. Run `/ec` in game.

## Contents

- [Why BetterEnderchests](#why-betterenderchests)
- [Features](#features)
- [Compatibility](#compatibility)
- [Installation](#installation)
- [Usage](#usage)
- [Commands](#commands)
- [Permissions](#permissions)
- [Configuration](#configuration)
- [Non-goals](#non-goals)
- [Building from source](#building-from-source)
- [Contributing](#contributing)
- [License](#license)

## Why BetterEnderchests

The vanilla Ender Chest is fixed at 27 slots, can't be shared and can't be opened without the block. BetterEnderchests replaces it with a virtual chest that you can size per rank, split into pages, share with other players and back up. It keeps working on every Minecraft version from 1.21.11 to 26.3 with a single JAR.

## Features

- **Open anywhere** with `/ec` or `/enderchest`, or by clicking a normal Ender Chest block.
- **Bigger chests**: 3 to 6 rows, set per rank or per player by an admin.
- **Pages**: up to 5 pages per player, unlocked by permission or assigned by an admin.
- **Sharing**: invite players to one page or all pages, and revoke access at any time.
- **Team chest**: a shared chest for your scoreboard team with `/teamec`.
- **Item blacklist**: block items such as bedrock or command blocks from being stored.
- **Backups**: manual and scheduled backups, a restore GUI, a safety backup before every restore, and optional Discord webhook alerts.
- **Repair**: recovers chests that fail to load because of items from a removed plugin.
- **Storage**: flat files or MySQL, with commands to move data between them.
- **Migration**: imports from vanilla Ender Chests, VariableEnderChests and QuickEC, and can export back to vanilla.
- **MiniMessage** for all messages. Legacy `&` and `§` colors are converted automatically on startup.
- **Folia support** and dupe protection that doesn't depend on the open cooldown.

## Compatibility

| BetterEnderchests | Minecraft | Server software | Java |
| --- | --- | --- | --- |
| 2.4.0 | 1.21.11, 26.1, 26.2, 26.3 | Paper, Folia and Paper forks such as Purpur and Leaf | 21 or newer |

One JAR covers every version in the table. Spigot and CraftBukkit are not supported.

Optional: VariableEnderChests or QuickEC, only if you want to import data from them.

## Installation

1. Make sure the server runs Paper or Folia 1.21.11 or newer on Java 21 or newer. Check with `/version`.
2. Download `BetterEnderchests.jar` from the [latest release](https://github.com/theprodogy/BetterEnderchests/releases/latest) and put it in `plugins/`.
3. Restart the server. Avoid `/reload`.
4. Edit `plugins/BetterEnderchests/config.yml` and `messages.yml`, then run `/ec reload`.

### Upgrading from EnderChest

Remove the old `EnderChest-*.jar` before you start the server; running both breaks the commands. On first start, `plugins/EnderChest/` is moved to `plugins/BetterEnderchests/` automatically, so all chests, invites and backups carry over.

### Using MySQL

Set the connection in `config.yml`, restart, then copy existing file data into the database:

```yaml
storage-type: mysql

mysql:
  host: "127.0.0.1"
  port: 3306
  database: "minecraft"
  username: "root"
  password: "change-me"
  table: "enderchest_data"
  use-ssl: false
  server-id: "server1"
```

```
/ec migrate to-db
```

## Usage

**Share your chest with a friend**

```
/ec invite Steve          # Steve can open page 1
/ec invite all Steve      # Steve can open every page
/ec uninvite Steve
```

Steve then opens it with `/ecshare <your name> [page]`.

**Give a player a bigger chest**

```
/ec setrows Steve 6       # 54 slots
/ec setpages Steve 3      # 3 pages
```

**Switch from the vanilla Ender Chest**

```
/ec backup                # take a backup first
/ec migrate vanilla       # imports every player's vanilla Ender Chest, online and offline
```

## Commands

### Players

| Command | Description |
| --- | --- |
| `/ec` | Open your Ender Chest. Alias: `/enderchest` |
| `/ec page <number>` | Open a specific page (when pages are enabled). |
| `/ec search <item>` | Find an item in your chest. |
| `/ec invite <player>` | Share page 1 or your standard chest. |
| `/ec invite <all\|number> <player>` | Share all pages or one page. |
| `/ec uninvite <player>` | Revoke a player's access. |
| `/ec invitelist` | List everyone you invited. |
| `/ecshare <player> [page]` | Open a chest you were invited to. |
| `/teamec` | Open your team's chest. Aliases: `/tec`, `/teamenderchest` |

### Admins

| Command | Description |
| --- | --- |
| `/ecsee <player> [page]` | Inspect any player's chest. Alias: `/enderchestsee` |
| `/ec reload` | Reload the config and messages. |
| `/ec setrows <player> <3-6>` / `/ec resetrows <player>` | Set or reset a player's rows. |
| `/ec setpages <player> <number>` / `/ec resetpages <player>` | Set or reset a player's pages. |
| `/ec rowinfo [player]` / `/ec pageinfo [player]` | Show row or page assignments. |
| `/ec clear <player> [page]` | Empty a player's chest. |
| `/ec clearall confirm` | Empty every chest. Cannot be undone. |
| `/ec backup` | Open the backup GUI. |
| `/ec backup <create\|restore\|delete> <name>` | Manage backups from chat. Restore takes `kick` or `live`. |
| `/ec autobackup <on\|off> [HH:mm]` | Toggle daily backups. |
| `/ec backupannounce <on\|off>` | Toggle backup chat announcements. |
| `/ec repair` | Recover chests that refuse to open. |
| `/ec migrate <vanilla\|to-db\|to-file\|from-vec\|quickec\|backups>` | Import or move data. |
| `/ec export` | Write chests back into vanilla player data. Items past slot 27 are lost. |

## Permissions

| Permission | Default | Purpose |
| --- | --- | --- |
| `ec.use` | Everyone | Open your own chest. |
| `ec.share` | Everyone | Open chests shared with you. |
| `ec.team` | Everyone | Use the team chest. |
| `ec.pages.1` … `ec.pages.5` | Page 1 only | Access each page. |
| `ec.see` | OP | Inspect other players' chests. |
| `ec.admin` | OP | All admin commands. |
| `ec.block` | Nobody | Stop a player from opening Ender Chests. |
| `ec.vanilla` | Nobody | Use the normal vanilla Ender Chest instead. |

Rank sizes in `ranks.groups` (for example `vip: 4`) apply to players with the `group.vip` permission. LuckPerms gives every member of a group that permission automatically.

## Configuration

`config.yml` is fully commented. The main sections:

| Section | Controls |
| --- | --- |
| `storage-type`, `mysql` | File or MySQL storage. |
| `size`, `ranks` | Default rows and rows per group. |
| `pages`, `admin-assigned-rows`, `admin-assigned-pages` | Pages and per-player overrides. |
| `teams` | Team chest on/off and size. |
| `blacklist` | Items that can't be stored. |
| `permissions` | Whether `ec.use` and `ec.block` are enforced. |
| `backups` | Auto backups, retention, safety backups, Discord webhook. |
| `sounds`, `opening`, `autosave` | Open/close sounds, open cooldown, autosave interval. |

`messages.yml` holds every player-facing message in MiniMessage format.

## Non-goals

- Spigot or CraftBukkit support. The plugin relies on Paper APIs.
- Minecraft versions older than 1.21.11.
- Changing gameplay beyond the Ender Chest itself.

## Building from source

Requires JDK 25. The output is Java 21 bytecode, so it still runs on 1.21.11 servers.

```bash
git clone https://github.com/theprodogy/BetterEnderchests.git
cd BetterEnderchests
./gradlew build
```

The server JAR is `build/libs/BetterEnderchests.jar`. Ignore `*-plain.jar`; it doesn't include the bundled NBT-API.

The build compiles against the 1.21.11 API. To check the code against the newest API:

```bash
./gradlew compileJava -PpaperApi=26.3.build.223-stable -PjavaRelease=25
```

## Contributing

Bug reports and pull requests are welcome.

- **Bugs**: open an [issue](https://github.com/theprodogy/BetterEnderchests/issues) with your server version (`/version`), the plugin version, the steps to reproduce and any console errors.
- **Pull requests**: keep one change per PR, make sure `./gradlew build` passes, and follow the existing code style. If the change touches commands, config or permissions, update this README in the same PR.

## License

BetterEnderchests is a fork and source reconstruction of [EnderChest by fernsehheft](https://modrinth.com/plugin/enderchest), built from `EnderChest-2.4.0.jar`. The original project is published as **All Rights Reserved**, so no open-source license is granted for this code. All rights to the original work belong to its author.
