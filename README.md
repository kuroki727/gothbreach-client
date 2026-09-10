# Gothbreach Client

A Fabric-based Minecraft cheat client, built for **1.20.4**. Originally aimed at 2b2t-style anarchy servers.

## Features

### Combat
- **KillAura** — auto-attacks nearby targets (configurable range, delay, auto-block, target priority)
- **CrystalAura** — automated end crystal PvP
- **AutoTotem** — swaps a totem into your offhand when needed
- **AutoArmor** — equips the best available armor automatically
- **Criticals** — forces critical hits
- **Reach** — extends attack/interaction range
- **Surround** — auto-places blocks around you for crystal PvP safety
- **AutoTrap** — auto-obsidian/bucket trapping
- **HoleFill** — fills holes around you automatically
- **Burrow** — digs down and seals you in
- **AutoLog** — disconnects automatically under configured conditions
- **AutoCity** — automated "city" block placement pattern
- **AutoPearl** — automatic ender pearl usage

### Movement
- **Speed**
- **Fly**
- **PacketFly** — packet-based fly that avoids some movement checks
- **NoSlow** — removes slowdown from eating, blocking, etc.
- **Step** — auto step-up
- **Jesus** — walk on water
- **Sprint** — forces sprinting
- **Strafe** — air strafing
- **ElytraFly** — motion control while gliding

More modules (ESP, X-Ray, Scaffold, Baritone pathing, etc.) exist as stubs in `ModuleManager` and will be enabled as they're finished.

## Requirements

- Java 17
- Minecraft 1.20.4
- [Fabric Loader](https://fabricmc.net/) `>=0.15.0`
- [Fabric API](https://modrinth.com/mod/fabric-api) `0.91.1+1.20.4`

## Building

```bash
git clone https://github.com/kuroki727/gothbreach-client.git
cd gothbreach-client
./gradlew build
```

The built jar will be in `build/libs/`.

## Installation

1. Install Fabric Loader for Minecraft 1.20.4.
2. Drop `fabric-api` and the built `cheat-client-*.jar` into your `.minecraft/mods` folder.
3. Launch the Fabric profile.

## Usage

Press **Right Shift** in-game to open the ClickGUI and toggle modules and their settings.

## Disclaimer

This client is intended for private/offline use and educational purposes. Using it on servers that prohibit client-side modifications will very likely get you banned. Use at your own risk.

## License

MIT
