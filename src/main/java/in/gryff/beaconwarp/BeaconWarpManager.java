package in.gryff.beaconwarp;

import com.google.common.math.DoubleMath;
import in.gryff.beaconwarp.config.BeaconWarpConfig;
import net.minecraft.block.Block;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraft.world.PersistentState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.world.ServerWorld;

import java.util.*;

public class BeaconWarpManager extends PersistentState {
    static int nextID = 1;
    static Map<List<Block>, Integer> beaconMap = new HashMap<>();
    static Map<Integer, List<MinecraftLocation>> channelMap = new HashMap<>();
    static Map<MinecraftLocation, Integer> blockMap = new HashMap<>(); //In the future this could be replaced with storing NBT data in the beacon

    public boolean registerBeacon(BlockPos pos, World world) {
        List<Block> baseBlockList = scanBase(pos, world);
        return registerWithScan(baseBlockList, pos, world);
    }

    public boolean registerWithScan(List<Block> baseBlockList, BlockPos pos, World world) {
        markDirty();
        if (baseBlockList.size() == 0)
            return false;
        //This register system will ASSUME that there is NO ENTRY for the beacon location in blockMap or channelMap.
        MinecraftLocation beaconLocation = new MinecraftLocation(pos, world.getRegistryKey());
        bwLog("--- Attempting to register beacon ---", 2);
        //printBase(parseBase(baseBlockList));

        if (!(beaconMap.get(baseBlockList) == null)) {
            bwLog("Beacon base already exists in list. Here's a list of all beacon block positions in that list:", 2);
            List<MinecraftLocation> destinations = channelMap.get(beaconMap.get(baseBlockList));
            for (MinecraftLocation location : destinations) {
                printBlockPos(location);
            }
            for (MinecraftLocation location : destinations) {
                if (location.equals(beaconLocation)) {
                    bwLog("Beacon already in list", 2);
                    return false;
                }
            }
        } else {
            addAllToList(baseBlockList);
        }

        int thisID = beaconMap.get(baseBlockList);
        blockMap.put(beaconLocation, thisID);
        bwLog(beaconLocation.toString(), 2);
        //System.out.println("Channel ID: " + thisID);
        if (!channelMap.containsKey(thisID)) {
            bwLog("Channel ID does not exist, opening channel with blank list.",2);
            List<MinecraftLocation> newList = new ArrayList<>();
            channelMap.put(thisID, newList);
        }

        //System.out.println("Adding to channel map");
        List<MinecraftLocation> thisList = channelMap.get(thisID);
        thisList.add(beaconLocation);
        channelMap.remove(thisID);
        channelMap.put(thisID, thisList);
        //System.out.println("If all went well, this beacon should be registered.");
        bwLog("--- Registry complete ---", 2);
        printFullMap();
        return true;
    }

    private void addAllToList(List<Block> baseBlockList) {
        bwLog("Beacon not already registered. Registering beacon, and checking. ID is " + nextID, 1);
        BeaconWarpConfig config = BeaconWarpConfig.getInstance();
        int numPasses = 1;
        boolean hasReflection = false;
        List<Block> newList = new ArrayList<>();

        if (config.allowRotate) {
            newList = rotateBase(baseBlockList);
            if (newList.equals(baseBlockList)) {
                numPasses = 1;
                bwLog("Base has four-fold rotational symmetry.", 3);
            } else {
                newList = rotateBase(newList);
                if (newList.equals(baseBlockList)) {
                    numPasses = 2;
                    bwLog("Base has two-fold rotational symmetry.", 3);
                } else {
                    numPasses = 4;
                    bwLog("Base has no rotational symmetry.", 3);
                }
            }
        }

        if (config.allowReflect){
            hasReflection = false;
            newList = reflectBase(baseBlockList);
            if (newList.equals(baseBlockList))
                hasReflection = true;
            if (numPasses == 4){
                newList = rotateBase(newList);
                if (newList.equals(baseBlockList))
                    hasReflection = true;
            }
        }

        beaconMap.put(baseBlockList, nextID);
        bwLog("pls work", 3);
        if (numPasses == 2){
            beaconMap.put(rotateBase(baseBlockList), nextID);
        }
        if (numPasses == 4){
            baseBlockList = rotateBase(baseBlockList);
            beaconMap.put((baseBlockList), nextID);
            baseBlockList = rotateBase(baseBlockList);
            beaconMap.put(rotateBase(baseBlockList), nextID);
            baseBlockList = rotateBase(baseBlockList);
            beaconMap.put(rotateBase(baseBlockList), nextID);
        }
        /*
        beaconMap.put(baseBlockList, nextID);
        //The beacon may have 4-fold symmetry, so we check for that.
        if (newList.equals(baseBlockList)) {
            //System.out.println("Base has 4-fold symmetry, cool!");
        } else {
            //System.out.println("Beacon appears to not have 4-fold symmetry... I hope?");
            beaconMap.put(newList, nextID);
            //printBase(parseBase(newList));
            newList = rotateBase(newList);
            //The beacon may have 2-fold symmetry, so we check for that, too
            if (newList.equals(baseBlockList)) {
                //System.out.println("Base has 2-fold symmetry, neat!");
            } else {
                //System.out.println("Beacon appears to not have 2-fold symmetry... I hope?");
                //printBase(parseBase(newList));
                beaconMap.put(newList, nextID);
                newList = rotateBase(newList);
                //printBase(parseBase(newList));
                beaconMap.put(newList, nextID);
                System.out.println("Okay, that's done... I hope?");
            }
        }
        */
        nextID += 1;
        bwLog("Base successfully registered in beacon map. Now for the channel map.", 2);
        printFullMap();
    }

    public static List<Block> scanBase(BlockPos pos, World world) {
        bwLog("Scanning base...", 2);
        List<Block> blockList = new ArrayList<>();
        List<Block> tempList = new ArrayList<>();
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        for (int l = 1; l <= 4; l++) {
            //l corresponds to the level we're checking. m and n will be the offset in x and y to check the corresponding layer.
            for (int m = -l; m <= l; m++) {
                for (int n = -l; n <= l; n++) {
                    if (!world.getBlockState(new BlockPos(i + m, j - l, k + n)).isIn(BlockTags.BEACON_BASE_BLOCKS)) {
                        tempList.clear();
                        bwLog("Base finished scanning", 2);
                        //printBase(parseBase(blockList));
                        //System.out.println("This beacon found an invalid block, so it's done scanning.");
                        return blockList;
                    }
                    Block currentBlock = world.getBlockState(new BlockPos(i + m, j - l, k + n)).getBlock();
                    tempList.add(currentBlock);
                }
            }
            bwLog("Beacon successfully scanned through tier " + l, 3);
            blockList.addAll(tempList);
            tempList.clear();
        }
        bwLog("Base successfully scanned.", 2);
        return blockList;
    }

    public static boolean checkValid(BlockPos pos, World world) {
        bwLog("Checking if beacon base is valid", 2);
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        //System.out.println("Beacon position: " + Integer.toString(i) + " " + Integer.toString(j) + " " + Integer.toString(k));
        for (int l = 0; l <= 8; l++) {
            //System.out.println("Checking block: " + Integer.toString(i+(l%3)-1) + " " + Integer.toString(j-1) + " " + Integer.toString(k+(l/3)-1));
            if (!world.getBlockState(new BlockPos(i + (l % 3) - 1, j - 1, k + (l / 3) - 1)).isIn(BlockTags.BEACON_BASE_BLOCKS)) {
                bwLog("Not valid", 2);
                return false;
            }
        }
        bwLog("Oh neat, it's valid", 3);
        return true;
    }

    public static List<Block> rotateList(List<Block> inputList) {
        //System.out.println("List being rotated. List size: " + inputList.size());
        int width = (int) Math.sqrt(inputList.size());
        List<Block> newList = new ArrayList<>(inputList);
        //Transpose the list, flipping the square over the diagonal axis
        for (int i = 0; i <= width - 1; i++) {
            for (int j = i + 1; j <= width - 1; j++) {
                Block tempBlock = newList.get((width * i) + j);
                newList.set((width * i) + j, newList.get((width * j) + i));
                newList.set((width * j) + i, tempBlock);
            }
        }
        //Flip sub-lists, flipping the square over the vertical axis
        newList = reflectList(newList);
        return newList;
    }

    public static List<Block> reflectList(List<Block> inputList) {
        int width = (int) Math.sqrt(inputList.size());
        List<Block> newList = new ArrayList<>(inputList);
        for (int i = 0; i <= width - 1; i++) {
            for (int j = 0; j <= ((width - 1) / 2) - 1; j++) {
                Block tempBlock = newList.get((width * i) + j);
                int second = (width * i) + (width - j - 1);
                newList.set((width * i) + j, newList.get(second));
                newList.set(second, tempBlock);
            }
        }
        return newList;
    }

    public static List<Block> rotateBase(List<Block> inputList) {
        List<List<Block>> blockList = parseBase(inputList);
        List<Block> outList = new ArrayList<>();
        for (List<Block> list : blockList) {
            outList.addAll(rotateList(list));
        }
        return outList;
    }

    public static List<Block> reflectBase(List<Block> inputList) {
        List<List<Block>> blockList = parseBase(inputList);
        List<Block> outList = new ArrayList<>();
        for (List<Block> list : blockList) {    
            outList.addAll(reflectList(list));
        }
        return outList;
    }   

    public static MinecraftLocation getBeaconTeleport(BlockPos pos, World world, List<Block> baseBlockList) {
        MinecraftLocation beaconLocation = new MinecraftLocation(pos, world.getRegistryKey());
        bwLog("Received information, attempting beacon warp", 2);
        if (!beaconMap.containsKey(baseBlockList)) {
            bwLog("Cannot teleport, block list not found!", 1);
            return beaconLocation;
        }
        List<MinecraftLocation> posList = channelMap.get(beaconMap.get(baseBlockList));
        for (int i = 0; i < posList.size(); i++) {
            MinecraftLocation location = posList.get(i);
            if (location.equals(beaconLocation)) {
                bwLog("Beacon is at index " + i + " of " + posList.size(), 3);
                bwLog("Returning teleport location...", 3);
                return posList.get((i + 1) % posList.size());
            }
        }
        bwLog("This beacon is not actually in the warp list! Cancelling teleport.", 2);
        return beaconLocation;
    }

    public List<Block> updateBeacon(BlockPos pos, World world) {
        MinecraftLocation beaconLocation = new MinecraftLocation(pos, world.getRegistryKey());
        List<Block> baseScan = scanBase(pos, world);
        Integer baseID = beaconMap.get(baseScan); //Integers can be null, ints will just be zero.
        Integer blockID = blockMap.get(beaconLocation);
        for (Map.Entry<MinecraftLocation, Integer> entry : blockMap.entrySet()) {
            if (entry.getKey().equals(beaconLocation)) {
                blockID = entry.getValue();
                break;
            }
        }
        if (world.getBlockState(pos).getBlock().getTranslationKey().equals("block.minecraft.beacon")) {
            bwLog("Beacon update found beacon.", 2);
            if (blockID == null) {
                if (baseID != null) {
                    bwLog("given blockID was null... why... Okay, let's check if it's in the system at all.", 3);
                    bwLog("So, here's our location: " + beaconLocation, 3);
                    for (Map.Entry<MinecraftLocation, Integer> entry : blockMap.entrySet()) {
                        MinecraftLocation entryLocation = entry.getKey();
                        bwLog(entryLocation.toString(), 3);
                        if (entry.getKey().equals(beaconLocation)) {
                            bwLog("Well shit, it's in the list... This means there's something wrong. VERY VERY WRONG.", 1);
                        }
                    }
                } else {
                    bwLog("Both baseID and blockID are null. THIS IS NOT A WARP BEACON.", 2);
                }
            } else if (baseID == null) {
                bwLog("baseID is null, we need to register this beacon again", 3);
                removeBeacon(beaconLocation, blockID);
                if (registerWithScan(baseScan, pos, world))
                    bwLog("UPDATE SUCCESSFUL!", 2);
            } else if (baseID.equals(blockID)) {
                //Beacon doesn't need to be updated
                bwLog("This beacon doesn't need to be updated.", 2);
            } else {
                bwLog("baseID and blockID are not equal! this beacon must be updated!", 3);
                bwLog("baseID: " + baseID + ", blockID: " + blockID, 3);
                removeBeacon(beaconLocation, blockID);
                if (registerWithScan(baseScan, pos, world))
                    bwLog("UPDATE SUCCESSFUL!", 2);
            }
        }
        return baseScan;
    }

    public void removeBeacon(MinecraftLocation beaconLocation, int channelID) {
        markDirty();
        //Removes a beacon from a given ID's channel, as well as from blockMap.
        List<MinecraftLocation> listInChannel = channelMap.get(channelID);
        List<MinecraftLocation> newList = new ArrayList<>();
        for (MinecraftLocation thisLocation : listInChannel) {
            if (!thisLocation.equals(beaconLocation))
                newList.add(thisLocation);
        }
        channelMap.remove(channelID);
        channelMap.put(channelID, newList);
        bwLog("Beacon successfully removed from channelMap", 2);
        Map<MinecraftLocation, Integer> blockMapCopy = new HashMap<>();
        for (Map.Entry<MinecraftLocation, Integer> entry : blockMap.entrySet()) {
            if (!(entry.getKey().equals(beaconLocation))) {
                blockMapCopy.put(entry.getKey(), entry.getValue());
            }
        }
        blockMap = blockMapCopy;
        bwLog("Beacon successfully removed from blockMap", 2);
    }

    public void removeBeaconWithLocation(BlockPos pos, World world) {
        //Only call when beacon is being broken

        //Removing a beacon at a given location might seem tricky, but it's not
        //If the beacon is a warp beacon, you can *always* clear the blockMap entry.
        //If the base has not changed, then its blockMap entry will be the same as the one that its base list corresponds to.
        //If the base has changed since it was registered, then either it's been updated or it hasn't.
        //If it's been updated since then, we have nothing to worry about, because the blockMap will match the channel map.
        //If it HASN'T been updated, then the changes aren't stored in the system, therefore deleting blockMap will remove its only entry.
        //This should be all we need to worry about at all.
        MinecraftLocation beaconLocation = new MinecraftLocation(pos, world.getRegistryKey());
        bwLog("Beacon is being broken at " + beaconLocation, 2);
        Integer beaconID = 0;
        for (Map.Entry<MinecraftLocation, Integer> entry : blockMap.entrySet()) {
            if (entry.getKey().equals(beaconLocation)) {
                beaconID = entry.getValue();
            }
        }
        if (beaconID == 0)
            bwLog("Oh no", 1);
        removeBeacon(beaconLocation, beaconID);
    }

    public boolean isWarpBeacon(BlockPos pos, World world) {
        MinecraftLocation beaconLocation = new MinecraftLocation(pos, world.getRegistryKey());
        Integer beaconID = null;
        for (Map.Entry<MinecraftLocation, Integer> entry : blockMap.entrySet()) {
            if (entry.getKey().equals(beaconLocation)) {
                beaconID = entry.getValue();
            }
        }
        bwLog("Is " + beaconLocation + " a warp beacon? " + (beaconID != null), 2);
        if (beaconID != null)
            bwLog("And the blockMap value is " + beaconID, 2);
        return beaconID != null;
    }

    public int countBeaconScore(List<Block> baseBlockList){
        bwLog("Counting beacon base score", 3);
        BeaconWarpConfig config = BeaconWarpConfig.getInstance();
        int score = 0;

        bwLog("Starting list", 3);
        for (int i = 0; i < baseBlockList.size(); i++) {
            Block baseBlock = baseBlockList.get(i);
            String tString = baseBlock.getTranslationKey();
            switch (tString) {
                case "block.minecraft.iron_block" -> score += config.Cooldown.scoreIron;
                case "block.minecraft.gold_block" -> score += config.Cooldown.scoreGold;
                case "block.minecraft.emerald_block" -> score += config.Cooldown.scoreEmerald;
                case "block.minecraft.diamond_block" -> score += config.Cooldown.scoreDiamond;
                case "block.minecraft.netherite_block" -> score += config.Cooldown.scoreNetherite;
            }
        }
        return score;
    }

    public int getCooldownTicks(float beaconScore){
        bwLog("Getting cooldown ticks", 3);
        //This math may not make sense at first, go to https://www.desmos.com/calculator/e3clqlhekj for the full derivation.
        //I could do this in one line but let's make this somewhat readable.
        BeaconWarpConfig config = BeaconWarpConfig.getInstance();
        float minCooldown = config.Cooldown.cooldownMinTicks;
        float maxCooldown = config.Cooldown.cooldownMaxTicks;
        float minScore = config.Cooldown.cooldownMinScore;
        float maxScore = config.Cooldown.cooldownMaxScore;

        double temp = beaconScore / minScore;
        double temp2 = maxScore / minScore;
        temp = DoubleMath.log2(temp) / DoubleMath.log2(temp2);
        temp = 1 - temp;
        double score = maxCooldown / minCooldown;
        score = Math.pow(score, temp);
        bwLog("cooldown ticks: " + (score * minCooldown), 3);
        return (int) Math.round(score * minCooldown);
    }

/*
    public boolean scoreValidWarp (int score){
        System.out.println("Score: " + score);
        BeaconWarpConfig config = BeaconWarpConfig.getInstance();
        if (score >= config.minTeleportScore)
            System.out.println("This is valid for teleportation");
        else
            System.out.println("This is NOT valid for teleportation");
        return (score >= config.minTeleportScore);
    }

    public boolean scoreValidInterdimensional (int score){
        System.out.println("Score: " + score);
        BeaconWarpConfig config = BeaconWarpConfig.getInstance();
        if (score >= config.minTeleportScore)
            System.out.println("This is valid for teleportation");
        else
            System.out.println("This is NOT valid for teleportation");
        return (score >= config.minInterdimensionalScore);
    }
*/

    public static List<List<Block>> parseBase(List<Block> blockList) {
        int size = blockList.size();
        if ((size != 9) && (size != 34) && (size != 83) && (size != 164)) {
            bwLog("UNCAUGHT ERROR! Beacon base has invalid length in parseBase!", 1);
        }
        List<List<Block>> outList = new ArrayList<>();
        int beaconLevelLimit = 4;
        for (int i = 1; i <= beaconLevelLimit; i++) {
            int startNum = (i * ((4 * i * i) - 1)) / 3 - 1;
            int endNum = ((i + 1) * ((4 * (i + 1) * (i + 1)) - 1)) / 3 - 1;
            if (size > startNum) { //(n*(4*n^2-1)/3)-1 is a mathematical expression for the number of total blocks in a beacon of size n. Thanks, OEIS!
                List<Block> newList = blockList.subList(startNum, endNum);
                outList.add(newList);
            } else
                i = beaconLevelLimit;
        }
        return outList;
    }

    public static void printList(List<Block> listIn) {
        int width = (int) Math.sqrt(listIn.size());
        for (int i = 0; i <= width - 1; i++) {
            StringBuilder outString = new StringBuilder();
            for (int j = 0; j <= width - 1; j++) {
                String tempString = listIn.get((width * i) + j).toString();
                outString.append(tempString, 6, tempString.length() - 1);
                if (j != width - 1)
                    outString.append(", ");
            }
            bwLog(outString.toString(), 2);
        }
    }

    public static void printBase(List<List<Block>> baseList) {
        for (List<Block> layer : baseList) {
            printList(layer);
        }
    }

    public static void printBlockPos(MinecraftLocation location) {
        bwLog(location.toString(), 2);
    }

    public static void printFullMap() {
        BeaconWarpConfig config = BeaconWarpConfig.getInstance();
        if (config.loggingType >= 2) {
            System.out.println("BW - A full map print has been called.");
            System.out.println(" ");
            System.out.println(" ");
            for (Map.Entry<List<Block>, Integer> entry : beaconMap.entrySet()) {
                System.out.println("---------------------------------------");
                printBase(parseBase(entry.getKey()));
                System.out.println("CORRESPONDS TO: " + entry.getValue());
            }
            System.out.println("---------------------------------------");
            System.out.println(" ");
            System.out.println(" ");
            System.out.println("Now for the channel map");
            System.out.println(" ");
            System.out.println(" ");
            for (Map.Entry<Integer, List<MinecraftLocation>> entry : channelMap.entrySet()) {
                System.out.println("---------------------------------------");
                System.out.println("Channel " + entry.getKey() + " corresponds to:");
                for (MinecraftLocation location : entry.getValue()) {
                    printBlockPos(location);
                }
            }
            System.out.println("---------------------------------------");
            System.out.println(" ");
            System.out.println(" ");
            System.out.println("Now for the block map");
            System.out.println(" ");
            System.out.println(" ");
            for (Map.Entry<MinecraftLocation, Integer> entry : blockMap.entrySet()) {
                System.out.println("MinecraftLocation " + entry.getKey() + " corresponds to " + entry.getValue());
            }
            System.out.println("---------------------------------------");
            System.out.println(" ");
            System.out.println(" ");
            System.out.println("Full print concluded.");
        } else {
            System.out.println("BW - Map print was called");
        }
    }

    public static BeaconWarpManager fromNbt(NbtCompound tag) {
        //Big thanks to Unascribed and other members of their discord guild for help regarding PersistentState and NBT
        bwLog("==readNbt called==", 2);
        printFullMap();
        BeaconWarpManager newManager = new BeaconWarpManager();

        //nextID
        nextID = tag.getInt("Beaconwarp:NextId");

        //beaconMap
        NbtList bMap = (NbtList) tag.get("Beaconwarp:BeaconMap");
        bwLog("beaconMap==========================", 3);
        bwLog(bMap.toString(), 3);
        for (net.minecraft.nbt.NbtElement nbtElement : bMap) {
            bwLog("new one", 4);
            NbtCompound thisEntry = (NbtCompound) nbtElement;
            NbtList thisList = (NbtList) thisEntry.get("bMapEntryListBlock");
            List<Block> newBlockList = new ArrayList<>();
            bwLog("blucks:", 4);
            for (int j = 0; j < thisList.size(); j++) {
                Identifier blockId = new Identifier(thisList.getString(j));
                newBlockList.add(Registry.BLOCK.get(blockId));
                bwLog(Registry.BLOCK.get(blockId).toString(), 4);
            }
            beaconMap.put(newBlockList, thisEntry.getInt("bMapEntryInteger"));
            bwLog("int:" + thisEntry.getInt("bMapEntryInteger"), 4);
        }

        //channelMap
        NbtList cMap = (NbtList) tag.get("Beaconwarp:ChannelMap");
        bwLog("channelMap==========================", 3);
        bwLog(cMap.toString(), 3);
        for (net.minecraft.nbt.NbtElement thisElement : cMap) {
            bwLog("new one", 4);
            NbtCompound thisEntry = (NbtCompound) thisElement;
            NbtList thisList = (NbtList) thisEntry.get("cMapEntryListMinecraftLocation");
            List<MinecraftLocation> newLocationList = new ArrayList<>();
            bwLog("locs:", 4);
            for (net.minecraft.nbt.NbtElement nbtElement : thisList) {
                newLocationList.add(MinecraftLocation.fromNbt((NbtCompound) nbtElement));
                bwLog(MinecraftLocation.fromNbt((NbtCompound) nbtElement).toString(), 4);
            }
            channelMap.put(thisEntry.getInt("cMapEntryId"), newLocationList);
            bwLog("int:" + thisEntry.getInt("cMapEntryId"), 4);
        }

        //blockMap
        NbtList blMap = (NbtList) tag.get("Beaconwarp:BlockMap");
        bwLog("blockMap==========================", 3);
        bwLog(blMap.toString(), 3);
        for (net.minecraft.nbt.NbtElement nbtElement : blMap) {
            bwLog("new one", 4);
            NbtCompound thisEntry = (NbtCompound) nbtElement;
            NbtCompound locationAsCompound = thisEntry.getCompound("blMapEntryListMinecraftLocation");
            MinecraftLocation thisLocation = MinecraftLocation.fromNbt(locationAsCompound);
            bwLog(thisLocation.toString() + thisEntry.getInt("blMapEntryId"), 4);
            blockMap.put(thisLocation, thisEntry.getInt("blMapEntryId"));
        }
        bwLog("==about to return==", 3);
        printFullMap();
        return newManager;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        bwLog("==writeNbt called==", 2);
        //nextID
        tag.put("Beaconwarp:NextId", NbtInt.of(nextID));

        //beaconMap
        NbtList bMapList = new NbtList();
        for (Map.Entry<List<Block>, Integer> mapEntry : beaconMap.entrySet()) {
            NbtCompound entryTag = new NbtCompound();
            NbtList blockList = new NbtList();
            for (Block b : mapEntry.getKey())
                blockList.add(NbtString.of(Registry.BLOCK.getId(b).toString()));
            entryTag.put("bMapEntryListBlock", blockList);
            entryTag.put("bMapEntryInteger", NbtInt.of(mapEntry.getValue()));
            bMapList.add(entryTag);
        }
        tag.put("Beaconwarp:BeaconMap", bMapList);
        bwLog(bMapList.toString(), 3);

        //channelMap
        NbtList cMapList = new NbtList();
        for (Map.Entry<Integer, List<MinecraftLocation>> mapEntry : channelMap.entrySet()) {
            NbtCompound entryTag = new NbtCompound();
            NbtList locationList = new NbtList();
            for (MinecraftLocation location : mapEntry.getValue())
                locationList.add(location.toNbt());
            entryTag.put("cMapEntryId", NbtInt.of(mapEntry.getKey()));
            entryTag.put("cMapEntryListMinecraftLocation", locationList);
            cMapList.add(entryTag);
        }
        tag.put("Beaconwarp:ChannelMap", cMapList);
        bwLog(cMapList.toString(), 3);

        //blockMap
        NbtList blMapList = new NbtList();
        for (Map.Entry<MinecraftLocation, Integer> mapEntry : blockMap.entrySet()) {
            NbtCompound entryTag = new NbtCompound();
            entryTag.put("blMapEntryListMinecraftLocation", mapEntry.getKey().toNbt());
            entryTag.put("blMapEntryId", NbtInt.of(mapEntry.getValue()));
            blMapList.add(entryTag);
        }
        tag.put("Beaconwarp:BlockMap", blMapList);
        bwLog(blMapList.toString(), 3);
        printFullMap();
        bwLog("==writeNbt over==", 2);
        return tag;
    }

    public static BeaconWarpManager clearedData(NbtCompound tag) {
        bwLog("==clearedData called==", 2);
        BeaconWarpManager manager = new BeaconWarpManager();
        nextID = 1;
        beaconMap.clear();
        channelMap.clear();
        blockMap.clear();
        return manager;
    }

    public BeaconWarpManager() {
        bwLog("==BeaconWarpManager constructor called==", 2);
    }

    public static BeaconWarpManager get(ServerWorld world) {
        bwLog("==get called==", 2);
        BeaconWarpConfig config = BeaconWarpConfig.getInstance();
        if (config.RESET_ALL_INFO) {
            bwLog("All beaconMap data will now be cleared.", 1);
            return world.getServer().getOverworld().getPersistentStateManager().getOrCreate(BeaconWarpManager::clearedData, BeaconWarpManager::new, "beaconwarp_manager");
        } else {
            bwLog("Will not clear data", 3);
        }
        return world.getServer().getOverworld().getPersistentStateManager().getOrCreate(BeaconWarpManager::fromNbt, BeaconWarpManager::new, "beaconwarp_manager");
    }

    public static void bwLog(String stringIn, int priority){
        //Priority types:
        //1: Very basic information to know the things necessary for functionality
        //2: Extra info about what functions are being called
        //3: Debug, which parts of functions are being called and what decisions are being made.
        //4: Extremely repetitive information about every part of the write and read functions... Shouldn't really be used like, ever.

        //This is an extremely dirty and bad method of logging but I can't be bothered to introduce a proper system yet.
        //At some point in the future this should all be ripped out and thrown into the fires of hell.
        BeaconWarpConfig config = BeaconWarpConfig.getInstance();
        if (priority <= config.loggingType)
            System.out.println("BW - "  + stringIn);
    }
}