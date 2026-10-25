# BetterEnderchests

BetterEnderchests gives Paper servers a configurable virtual Ender Chest. Players can open their own storage with `/ec`, unlock additional pages, share access with other players, and use a separate team chest. Server owners can control chest size, access, item restrictions, backups, and data migration through configuration.

## Features

- Open a personal Ender Chest with `/ec` or `/enderchest`.
- Give players multiple storage pages with `ec.pages.*` permissions or administrator-assigned pages.
- Share a chest with invited players, or inspect another player’s chest as an administrator.
- Provide a shared team chest with `/teamec`.
- Set chest sizes by rank, blacklist items, and let selected players use the vanilla Ender Chest instead.
- Search stored items and manage backups, restores, exports, automatic backups, and migrations.
- Configure messages with MiniMessage. Legacy `&` and `§` formatting is migrated when the plugin starts.

## Compatibility

One JAR supports Paper and Folia from 1.21.11 through 26.3, on Java 21 or newer.

## Installation

1. Download `BetterEnderchests.jar` from the [latest release](https://github.com/theprodogy/BetterEnderchests/releases/latest).
2. Place the JAR in the server’s `plugins` directory.
3. Restart the server.
4. Upgrading from EnderChest: remove the old JAR. The existing `plugins/EnderChest` folder is moved to `plugins/BetterEnderchests` on first start.
5. Edit the generated `plugins/BetterEnderchests/config.yml` and `messages.yml` to fit the server.

## Commands

| Command | Description |
| --- | --- |
| `/ec` | Open your personal Ender Chest. |
| `/ec invite <player>` | Invite a player to your chest. |
| `/ec uninvite <player>` | Remove a player’s access. |
| `/ec invitelist` | List players with access. |
| `/ec search <query>` | Search your stored items. |
| `/ec page <number>` | Open one of your storage pages. |
| `/ecshare <player> [page]` | Open a player’s chest when you have access. |
| `/ecsee <player> [page]` | Inspect a player’s chest as an administrator. |
| `/teamec` | Open your team’s Ender Chest. |

Administrators can also use `/ec reload`, `/ec migrate`, `/ec export`, `/ec backup`, and `/ec autobackup`.

## Permissions

| Permission | Default | Purpose |
| --- | --- | --- |
| `ec.use` | Everyone | Open a personal chest. |
| `ec.share` | Everyone | Open chests shared with the player. |
| `ec.team` | Everyone | Use the team chest. |
| `ec.pages.1` through `ec.pages.5` | Page 1 only | Access storage pages. |
| `ec.see` | Operators | Inspect other players’ chests. |
| `ec.admin` | Operators | Reload settings, migrate data, and manage slots. |
| `ec.block` | Nobody | Prevent a player from opening Ender Chests. |
| `ec.vanilla` | Nobody | Use the standard vanilla Ender Chest. |

## Configuration

`config.yml` controls rank-based sizes, team storage, blacklisted items, page limits, and backup settings. `messages.yml` contains all player-facing text.

## Development

Build with JDK 25 (the output is Java 21 bytecode):

```bash
./gradlew build
```

The deployable shaded JAR is written to `build/libs/BetterEnderchests.jar`. It compiles against the 1.21.11 API; to check against the newest API run `./gradlew compileJava -PpaperApi=26.3.build.223-stable -PjavaRelease=25`.

## Upstream project

This project is a fork and reconstruction of [EnderChest by fernsehheft](https://modrinth.com/plugin/enderchest), based on `EnderChest-2.4.0.jar`.
