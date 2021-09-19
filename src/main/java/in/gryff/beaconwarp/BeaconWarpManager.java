package in.gryff.beaconwarp;

import net.minecraft.block.Block;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

public class BeaconWarpManager {
    static int nextID = 1;
    static Map<List<Block>, Integer> beaconMap = new HashMap<>();
    static Map<Integer, List<MinecraftLocation>> channelMap = new HashMap<>();
    static Map<MinecraftLocation, Integer> blockMap = new HashMap<>(); //In the future this could be replaced with storing NBT data in the beacon

    public static boolean registerBeacon(BlockPos pos, World world) {
        List<Block> baseBlockList = scanBase(pos, world);
        return registerWithScan(baseBlockList, pos, world);
    }

    public static boolean registerWithScan(List<Block> baseBlockList, BlockPos pos, World world) {
        if (baseBlockList.size() == 0)
            return false;
        //This register system will ASSUME that there is NO ENTRY for the beacon location in blockMap or channelMap.
        MinecraftLocation beaconLocation = new MinecraftLocation(pos, world.getRegistryKey());
        System.out.println("--- Attempting to register beacon ---");
        //printBase(parseBase(baseBlockList));
        if (!(beaconMap.get(baseBlockList) == null)) {
            System.out.println("Beacon base already exists in list. Here's a list of all beacon block positions in that list:");
            List<MinecraftLocation> destinations = channelMap.get(beaconMap.get(baseBlockList));
            for (MinecraftLocation location : destinations) {
                printBlockPos(location);
            }
            for (MinecraftLocation location : destinations) {
                if (location.equals(beaconLocation)) {
                    System.out.println("Beacon already in list");
                    return false;
                }
            }
        } else {
            System.out.println("Beacon not already registered. Registering beacon, and checking. ID is " + nextID);
            beaconMap.put(baseBlockList, nextID);
            //The beacon may have 4-fold symmetry, so we check for that.
            List<Block> newList = new ArrayList<>(rotateBase(baseBlockList));
            if (newList.equals(baseBlockList)) {
                //System.out.println("Base has 4-fold symmetry, cool!");
            } else {
                //System.out.println("Beacon appears to not have 4-fold symmetry... I hope?");
                beaconMap.put(newList, nextID);
                printBase(parseBase(newList));
                newList = rotateBase(newList);
                //The beacon may have 2-fold symmetry, so we check for that, too
                if (newList.equals(baseBlockList)) {
                    //System.out.println("Base has 2-fold symmetry, neat!");
                } else {
                    //System.out.println("Beacon appears to not have 2-fold symmetry... I hope?");
                    printBase(parseBase(newList));
                    beaconMap.put(newList, nextID);
                    newList = rotateBase(newList);
                    printBase(parseBase(newList));
                    beaconMap.put(newList, nextID);
                    System.out.println("Okay, that's done... I hope?");
                }
            }
            nextID += 1;
            System.out.println("Base successfully registered in beacon map. Now for the channel map.");
        }
        int thisID = beaconMap.get(baseBlockList);
        blockMap.put(beaconLocation, thisID);
        //System.out.println("Channel ID: " + thisID);
        if (!channelMap.containsKey(thisID)) {
            System.out.println("Channel ID does not exist, opening channel with blank list.");
            List<MinecraftLocation> newList = new ArrayList<>();
            channelMap.put(thisID, newList);
        }
        //System.out.println("Adding to channel map");
        List<MinecraftLocation> thisList = channelMap.get(thisID);
        thisList.add(beaconLocation);
        channelMap.remove(thisID);
        channelMap.put(thisID, thisList);
        //System.out.println("If all went well, this beacon should be registered.");
        System.out.println("--- Registry complete ---");
        //printFullMap();
        return true;
    }

    public static List<Block> scanBase(BlockPos pos, World world) {
        System.out.println("Scanning base...");
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
                        System.out.println("Base finished scanning");
                        //printBase(parseBase(blockList));
                        //System.out.println("This beacon found an invalid block, so it's done scanning.");
                        return blockList;
                    }
                    Block currentBlock = world.getBlockState(new BlockPos(i + m, j - l, k + n)).getBlock();
                    tempList.add(currentBlock);
                }
            }
            System.out.println("Beacon successfully scanned through tier " + l);
            blockList.addAll(tempList);
            tempList.clear();
        }
        System.out.println("Base successfully scanned.");
        return blockList;
    }

    public static boolean checkValid(BlockPos pos, World world) {
        System.out.println("Checking if beacon base is valid");
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        //System.out.println("Beacon position: " + Integer.toString(i) + " " + Integer.toString(j) + " " + Integer.toString(k));
        for (int l = 0; l <= 8; l++) {
            //System.out.println("Checking block: " + Integer.toString(i+(l%3)-1) + " " + Integer.toString(j-1) + " " + Integer.toString(k+(l/3)-1));
            if (!world.getBlockState(new BlockPos(i + (l % 3) - 1, j - 1, k + (l / 3) - 1)).isIn(BlockTags.BEACON_BASE_BLOCKS)) {
                System.out.println("Not valid");
                return false;
            }
        }
        System.out.println("Oh neat, it's valid");
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
        for (int i = 0; i <= width - 1; i++) {
            for (int j = 0; j <= ((width - 1) / 2) - 1; j++) {
                Block tempBlock = newList.get((width * i) + j);
                int second = (width * i) + (width - j - 1);
                newList.set((width * i) + j, newList.get(second));
                newList.set(second, tempBlock);
            }
        }
        //System.out.println("List done being rotated. List size: " + inputList.size());
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

    public static MinecraftLocation getBeaconTeleport(BlockPos pos, World world, List<Block> baseBlockList) {
        MinecraftLocation beaconLocation = new MinecraftLocation(pos, world.getRegistryKey());
        System.out.println("Received information, attempting beacon warp");
        if (!beaconMap.containsKey(baseBlockList)) {
            System.out.println("Cannot teleport, block list not found!");
            return beaconLocation;
        }
        List<MinecraftLocation> posList = channelMap.get(beaconMap.get(baseBlockList));
        for (int i = 0; i < posList.size(); i++) {
            MinecraftLocation location = posList.get(i);
            if (location.equals(beaconLocation)) {
                System.out.println("Beacon is at index " + i + " of " + posList.size());
                System.out.println("Returning teleport location...");
                return posList.get((i + 1) % posList.size());
            }
        }
        System.out.println("This beacon is not actually in the warp list! Cancelling teleport.");
        return beaconLocation;
    }

    public static List<Block> updateBeacon(BlockPos pos, World world) {
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
            System.out.println("Beacon update found beacon.");
            if (blockID == null) {
                if (baseID != null) {
                    System.out.println("given blockID was null... why... Okay, let's check if it's in the system at all.");
                    System.out.println("So, here's our location: " + beaconLocation);
                    for (Map.Entry<MinecraftLocation, Integer> entry : blockMap.entrySet()) {
                        System.out.println(entry.getKey());
                        if (entry.getKey().equals(beaconLocation)) {
                            System.out.println("Well shit, it's in the list... This means there's something wrong. VERY VERY WRONG.");
                        }
                    }
                } else {
                    System.out.println("Both baseID and blockID are null. THIS IS NOT A WARP BEACON.");
                }
            } else if (baseID == null) {
                System.out.println("baseID is null, we need to register this beacon again");
                removeBeacon(beaconLocation, blockID);
                if (registerWithScan(baseScan, pos, world))
                    System.out.println("UPDATE SUCCESSFUL!");
            } else if (baseID.equals(blockID)) {
                //Beacon doesn't need to be updated
                System.out.println("This beacon doesn't need to be updated.");
            } else {
                System.out.println("baseID and blockID are not equal! this beacon must be updated!");
                System.out.println("baseID: " + baseID + ", blockID: " + blockID);
                removeBeacon(beaconLocation, blockID);
                if (registerWithScan(baseScan, pos, world))
                    System.out.println("UPDATE SUCCESSFUL!");
            }
        }
        return baseScan;
    }

    public static void removeBeacon(MinecraftLocation beaconLocation, int channelID) {
        //Removes a beacon from a given ID's channel, as well as from blockMap.
        List<MinecraftLocation> listInChannel = channelMap.get(channelID);
        List<MinecraftLocation> newList = new ArrayList<>();
        for (MinecraftLocation thisLocation : listInChannel) {
            if (!thisLocation.equals(beaconLocation))
                newList.add(thisLocation);
        }
        channelMap.remove(channelID);
        channelMap.put(channelID, newList);
        System.out.println("Beacon successfully removed from channelMap");
        Map<MinecraftLocation, Integer> blockMapCopy = new HashMap<>();
        for (Map.Entry<MinecraftLocation, Integer> entry : blockMap.entrySet()) {
            if (!(entry.getKey().equals(beaconLocation))) {
                blockMapCopy.put(entry.getKey(), entry.getValue());
            }
        }
        blockMap = blockMapCopy;
        System.out.println("Beacon successfully removed from blockMap");
    }

    public static void removeBeaconWithLocation(BlockPos pos, World world){
        //Only call when beacon is being broken

        //Removing a beacon at a given location might seem tricky, but it's not
        //If the beacon is a warp beacon, you can *always* clear the blockMap entry.
        //If the base has not changed, then its blockMap entry will be the same as the one that its base list corresponds to.
        //If the base has changed since it was registered, then either it's been updated or it hasn't.
        //If it's been updated since then, we have nothing to worry about, because the blockMap will match the channel map.
        //If it HASN'T been updated, then the changes aren't stored in the system, therefore deleting blockMap will remove its only entry.
        //This should be all we need to worry about at all.
        MinecraftLocation beaconLocation = new MinecraftLocation(pos, world.getRegistryKey());
        System.out.println("Beacon is being broken at " + beaconLocation);
        Integer beaconID = 0;
        for (Map.Entry<MinecraftLocation, Integer> entry : blockMap.entrySet()) {
            if (entry.getKey().equals(beaconLocation)) {
                beaconID = entry.getValue();
            }
        }
        if (beaconID == 0)
            System.out.println("Oh no");
        removeBeacon(beaconLocation, beaconID);
    }

    public static boolean isWarpBeacon(BlockPos pos, World world){
        MinecraftLocation beaconLocation = new MinecraftLocation(pos, world.getRegistryKey());
        Integer beaconID = null;
        for (Map.Entry<MinecraftLocation, Integer> entry : blockMap.entrySet()) {
            if (entry.getKey().equals(beaconLocation)) {
                beaconID = entry.getValue();
            }
        }
        System.out.println("Is " + beaconLocation + " a warp beacon? " + (beaconID != null));
        return beaconID != null;
    }

    public static List<List<Block>> parseBase(List<Block> blockList) {
        int size = blockList.size();
        if ((size != 9) && (size != 34) && (size != 83) && (size != 164)) {
            System.out.println("UNCAUGHT ERROR! Beacon base has invalid length in parseBase!");
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
            System.out.println(outString);
        }
    }

    public static void printBase(List<List<Block>> baseList) {
        for (List<Block> layer : baseList) {
            printList(layer);
        }
    }

    public static void printBlockPos(MinecraftLocation location) {
        System.out.println(location.toString());
    }

    public static void printFullMap() {
        System.out.println("A full map print has been called.");
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
    }

}
