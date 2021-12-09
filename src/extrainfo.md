# Extra Information

This is a file that's meant to contain tons of information, ideas, and concepts related to BeaconWarp.
Whatever wouldn't be appropriate in comments or gitHub commit logs will go here!

It'll also function as an update log, telling what sections have been added to the "concepts" section.
As things change with Beacon Warp, new versions will be added to the top of this file, below here.
Alllllll the information. Let's go!

# Changelog

### Patch 0.7.1

Changelog:
- Implemented config options are now functional.
- Reorganized beacon registration code
  - Now generalizes reflections into their own method, prepping for adding reflections.
  - Separates adding new beaconMap entries into its own method

Meta:
  - New "Symmetry" section. 

## The story as of now (0.7.0)

Right now, the Beacon Warp source code spans 8 files, excluding assets.

The most important file is BeaconWarpManager.
This class comprises the VAST majority of code (518 lines out of a total of 828)
Here's the general purpose of each of the files and their length, sorted roughly by importance
(length doesn't really matter, at least in terms of the complexity of the role that that file serves)

- BeaconWarpManager (518): Store and process all the data for beacon warps
- MinecraftLocation (59): A custom type which stores a block's coordinates and its world
- ServerPlayerMixin (90): Detect when a player walks on a beacon and teleport them
- BeaconBlockMixin (55): Detect when a beacon should be added to a warp list
- BlockMixin (28): Detect when a beacon is broken and should be removed from a warp list
- BeaconWarp (15): Initialize the mod
- BeaconWarpConfig (65): Hold and provide settings for the mod
- BeaconWarpModMenuCompatibility (18): Make settings accessible via Mod Menu (in singleplayer)

Before delving into the way each of these works (where necessary), here's the basic ideas behind how the mod functions!

#Concepts
## Links and chains

The general idea of the mod is that locations will be linked together in chains, 
rather than added into a large pool that can all be accessed from each other.
The pattern on the base of the beacon determines which other warp beacons it will link to!
Beacons with the same pattern all link to each other. 
Standing on a warp beacon will take you to the beacon at the next link in the chain.

So how would one go about storing these?

To start off, if you simply stored a list of blocks and had that list correspond to the list of locations, that would
just be... Very difficult to work with. It would mean that if a given beacon base had valid rotations, then you'd have 
to update the list of locations in every single one of those, *and* it would be very space inefficient.

So we don't store it that way, of course. Here's how we DO store it!
First, we have one `Map` which maps a `List` of `Block`s to an `Integer`. This is called beaconMap.  
Then, we have another `Map` which maps an `Integer` to a `List` of `MinecraftLocation`s. This is called channelMap.  
This way, each beacon's base is stored as a `List<Block>` which corresponds to an integer. 
The integer, in both cases, is called the channel ID.
This way, we can have all the valid rotations and reflections of a certain beacon base correspond directly to an integer.  
The chains are stored as `List<MinecraftLocation>`.

When someone steps on a beacon, we scan the base, then find the channelID of that beacon base.
Then we check the channelMap and get the list of minecraft locations. 
We scan that list to find the position (index) of our current location, and then we just return the next location in
the chain. If our current location is the last one in the chain then we return the first in the chain.

## Symmetry

So, basically, if you have a square grid of blocks, the distribution of blocks in that grid can have different properties:  
- Reflected symmetry
- Rotated symmetry

But we need to be more specific than this, actually:  
Rotational symmetry has 3 types:  
Grids that are unique any way you rotate them,  
Grids that are unique when you rotate 90° but not 180°, and  
Grids that are not unique any way you rotate them.  

There are also 4 types of reflected symmetry:
Grids that are the same when you flip them horizontally,
Grids that are the same when you flip them vertically,
Grids that are the same when you do EITHER of those, and
Grids that are never the same no matter the flips.

The thing is, these types are all linked to each other.
Flipping something horizontally AND vertically is the same as rotating something 180°!
In fact, one-flip mirror symmetry is all there is. 
Two-flip just brings it back to the original, unless there's some kind of rotation involved.

So to be clear, here are ALL the different valid types of symmetry groupings:
- `A1` Grids unique after ANY transformation
- `A2` Grids unique after 90° rotation or reflection, but not after 180°
- `A3` Grids not unique after any rotation, but unique after reflection
- `B1` Grids unique after 90° or 180°, but not after reflection 
- `C2` Grids unique after 90°, but not 180° or reflection
- `C3` Grids not unique after ANY transformation

Os represent that, after that type of transformation, the grid type is unchanged

| Name | flip | 180° | 90°|
| ---|---|---|---|
| A1 | X | X | X |
| A2 | X | O | X |
| A3 | X | O | O |
| B1 | O | X | X |
| C2 | O | O | X |
| C3 | O | O | O |
One with an eye for binary will notice that two things might be missing!
But, as it turns out, those two would be situtations where 90° rotations would produce unique grids,
but 180° rotations would not. Such a thing cannot happen!

Our config options are "allow rotations" and "allow reflections".  
The way this will be implemented, both of these will allow 180° rotations!  
It's obvious to see why allowing 90° rotations must allow 180° rotations,
but if we allow mirrored reflections, 

# The future...?

This section is dedicated to ideas that may, or may not, make it into beacon warp eventually.
This is meant to be for features, which are usually optional, so the ideas won't mention whether they're optional or not!
It's implied that all of these are optional.

Idea list:
- [ ] Fuel to use a beacon warp
- [ ] Localized beacon networks, possibly tree-based?
- [ ] Composition of beacon determines cooldown
- [ ] Composition of beacon determines range of beacon
- [ ] Locking interdimensional travel behind certain materials
- [ ] Locking travel to each specific dimension behind specific beacon material requirements
- [ ] One-time warp tokens, bound to specific beacons (a nether star with NBT?)
- [ ] Particles!!!! LOTS of particles to indicate successes and failures!!!
- [ ] Ability to configure valid types of warp base blocks
- [ ] Separate cooldown for using the same beacon as warp
- [ ] Add option to ban offensive imagery in warps (like swastikas)

Internal idea list:
- [ ] Network to JSON exporter/importer. Would need to include the config file settings as part of it.
- [ ] Custom type for beacon bases. Needs a .equals which can check for rotations and reflections, 
hashcode implementation, and a converter that takes it from itself to NBT and back.