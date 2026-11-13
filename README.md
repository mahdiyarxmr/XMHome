# XMHome

**XMHome** is a premium, modular Minecraft home-management plugin for Paper servers (1.8.8 through 1.21+), featuring multiple Home Sets, multi-world & cross-dimensional teleportation, Vault economy integration with dynamic progressive pricing, optional GriefPrevention integration, English & Finglish (`en` / `fa`) languages, SQLite/MySQL persistence, and a dark-fantasy Inventory GUI.

## Artifacts & Compatibility Matrix

| Artifact | Supported Minecraft Versions | Java Target |
|---|---|---|
| `XMHome-Legacy.jar` | Paper / Spigot 1.8.8 – 1.12.2 | Java 8 |
| `XMHome-Modern.jar` | Paper 1.13 – 1.21+ | Java 17+ |

## Building from Source

Requirements:
- JDK 17+ (to build both `xmhome-legacy` with target 1.8 and `xmhome-modern` with target 17)
- Apache Maven 3.8+

```bash
mvn clean package
```

Outputs:
- `xmhome-legacy/target/XMHome-Legacy.jar`
- `xmhome-modern/target/XMHome-Modern.jar`

## Installation

1. Download or build the appropriate JAR for your server version (`XMHome-Legacy.jar` or `XMHome-Modern.jar`).
2. Place the JAR in your server's `plugins/` folder.
3. (Optional) Install **Vault** + an economy provider, **GriefPrevention**, **LuckPerms**, and/or **PlaceholderAPI**.
4. Start the server to generate `plugins/XMHome/config.yml` and `plugins/XMHome/languages/`.

## Commands

- `/sethome [name]` or `/sethome <set> <name>` — Create a home
- `/home [name]` — Teleport to a home (or open GUI)
- `/home duplicate <source> <new>` — Duplicate a home
- `/homes` — Open the main Homes GUI
- `/delhome <name>` — Delete a home
- `/homesets` — Open the Home Sets GUI
- `/homeset <create|delete|rename|select|list>` — Manage Home Sets
- `/homecoords <name>` — Display/copy home coordinates
- `/homeshare <home> <player> [VISIT|VIEW|MANAGE]` — Share a home
- `/homeunshare <home> <player>` — Revoke home sharing
- `/homeinfo <name>` — View home information
- `/homefavorite <name>` — Toggle favorite status
- `/homeadmin` — Open Admin GUI
- `/xmhome <reload|version|debug>` — Admin utilities
