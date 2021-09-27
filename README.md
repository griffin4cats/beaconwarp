# Beacon Warp

## Balanced fast travel

Beacon warp is a server-side that adds fast travel capability to beacons, with a goal of providing an intuitive yet balanced method of fast travel.

To activate a beacon's warp capabilities, right click a beacon with dragon's breath.

The composition and configuration of the base of the beacon determines its channel. In other words, beacons will only link to other beacons that are constructed with an identical base.
Instead of warp selectors, beacons are linked together in loops, so each beacon of a given base will only teleport you to the next in the chain.

These two ideas combined together makes for a simple but balanced fast travel system.

## Why beacon warp?

Vanilla minecraft has no method of fast travel besides nether highways. However, the infrastructure this requires is enormous.

Warp mods that add commands like /home that instantly let you travel to places are essentially a watered down version of /teleport, and they don't encourage any form of infrastructure in the world.

Beacon warp strikes a balance between these two.
By adding a cooldown between teleports, it discourages linking huge networks that connect your entire base. If it takes 10 seconds for your teleport cooldown to expire, and you have 7 different places in your base linked with beacons, then it could take up to a minute to reach the destination you want to go to. 

But it *does* encourage infrastructure, in the form of warp hubs. They'll let you link an arbitrary amount of locations together, with only a 10 second travel time between them.

## What's next?

There's a few important things left to do before release.

- [x] Interdimensional travel
- [x] Updating beacons
- [x] Checking beacon at destination
- [x] Warp deletion
- [x] Making singleplayer compatibility
- [x] Saving data
- [x] Config file*
- [ ] Big code cleanup/optimization

**functional, but not implemented yet*

These are the essential things left before release, but I also have a number of tweaks and extra features that I might decide to add before I release this.

##Credits
Partially inspired by Maxypoo's [Warp shrines](https://www.planetminecraft.com/data-pack/warp-shrines-teleport-between-locations/).
Also, check out GatKong's [TeleBeacon datapack](https://www.planetminecraft.com/data-pack/telebeacon/)!

Textures in mod icon are modified from Ewan Howell's [F8thful](https://www.ewanhowell.com/?pack=f8thful)

Big thanks to Unascribed and those in their discord guild for all their help.