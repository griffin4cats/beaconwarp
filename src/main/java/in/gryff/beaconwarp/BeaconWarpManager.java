package in.gryff.beaconwarp;

import net.minecraft.block.Block;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class BeaconWarpManager {
    static int nextID = 0;
    static Map<List<Block>, Integer> beaconMap = new HashMap<>();
    static Map<Integer, List<MinecraftLocation>> channelMap = new HashMap<>();

    public static boolean registerBeacon(BlockPos beaconPos, World world){
        MinecraftLocation beaconLocation = new MinecraftLocation(beaconPos, world.getRegistryKey());
        System.out.println("--- Attempting to register beacon ---");
        List<Block> baseBlockList = scanBase(beaconPos, world);
        System.out.println("Here's the new list! ");
        printBase(parseBase(baseBlockList));
        if (!(beaconMap.get(baseBlockList) == null)){
            System.out.println("Beacon base already exists in list. Here's a list of all beacon block positions in that list:");
            List<MinecraftLocation> destinations = channelMap.get(beaconMap.get(baseBlockList));
            for (MinecraftLocation location: destinations){
                printBlockPos(location);
            }
            for (MinecraftLocation location: destinations){
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
            //System.out.println("Now here's the two lists i guess:");
            //printBase(parseBase(baseBlockList));
            //printBase(parseBase(newList));
            if (newList.equals(baseBlockList)) {
                System.out.println("Base has 4-fold symmetry, cool!");
            } else {
                System.out.println("Beacon appears to not have 4-fold symmetry... I hope?");
                beaconMap.put(newList, nextID);
                printBase(parseBase(newList));
                newList = rotateBase(newList);
                //The beacon may have 2-fold symmetry, so we check for that, too
                if (newList.equals(baseBlockList)) {
                    System.out.println("Base has 2-fold symmetry, neat!");
                } else{
                    System.out.println("Beacon appears to not have 2-fold symmetry... I hope?");
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
        System.out.println("Channel ID: " + thisID);
        if (!channelMap.containsKey(thisID)) {
            System.out.println("Channel ID does not exist, opening channel with blank list.");
            List<MinecraftLocation> newList = new ArrayList<>();
            channelMap.put(thisID, newList);
        }
        System.out.println("Adding to channel map");
        List<MinecraftLocation> thisList = channelMap.get(thisID);
        thisList.add(beaconLocation);
        channelMap.remove(thisID);
        channelMap.put(thisID, thisList);
        System.out.println("If all went well, this beacon should be registered.");
        printFullMap();
        return true;
    }

    public static List<Block> scanBase(BlockPos pos, World world){
        System.out.println("Scanning base...");
        //Returns a list of all the valid blocks in the beacon base.
        List<Block> blockList = new ArrayList();
        List<Block> tempList = new ArrayList();
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        for (int l = 1; l <= 4; l++){
            //l corresponds to the level we're checking. m and n will be the offset in x and y to check the corresponding layer.
            for (int m = -l; m <= l; m++){
                for (int n = -l; n <= l; n++) {
                    if (!world.getBlockState(new BlockPos(i+m, j-l, k+n)).isIn(BlockTags.BEACON_BASE_BLOCKS)) {
                        tempList.clear();
                        System.out.println("Base finished scanning");
                        return blockList;
                    }
                    Block currentBlock = world.getBlockState(new BlockPos(i+m, j-l, k+n)).getBlock();
                    tempList.add(currentBlock);
                }
            }
            blockList.addAll(tempList);
            tempList.clear();
        }
        System.out.println("Base successfully scanned.");
        return blockList;
    }

    public static boolean checkValid(BlockPos pos, World world){
        System.out.println("Checking if beacon base is valid");
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        //System.out.println("Beacon position: " + Integer.toString(i) + " " + Integer.toString(j) + " " + Integer.toString(k));
        for (int l = 0; l <= 8; l++){
            //System.out.println("Checking block: " + Integer.toString(i+(l%3)-1) + " " + Integer.toString(j-1) + " " + Integer.toString(k+(l/3)-1));
            if (!world.getBlockState(new BlockPos(i+(l%3)-1, j-1, k+(l/3)-1)).isIn(BlockTags.BEACON_BASE_BLOCKS)){
                System.out.println("Not valid");
                return false;
            }
        }
        System.out.println("Oh neat, it's valid");
        return true;
    }

    public static List<Block> rotateList(List<Block> inputList){
        //System.out.println("List being rotated. List size: " + inputList.size());
        int width = (int) Math.sqrt(inputList.size());
        List<Block> newList = new ArrayList<>();
        newList.addAll(inputList);
        //Transpose the list, flipping the square over the diagonal axis
        for (int i = 0; i <= width-1; i++){
            for (int j = i+1; j <= width-1; j++){
                Block tempBlock = newList.get((width*i) + j);
                newList.set((width*i) + j, newList.get((width*j) + i));
                newList.set((width*j) + i, tempBlock);
            }
        }
        //Flip sublists, flipping the square over the vertical axis
        for (int i = 0; i <= width-1; i++){
            for (int j = 0; j <= ((width-1)/2)-1; j++){
                Block tempBlock = newList.get((width*i) + j);
                newList.set((width*i) + j, newList.get((width*i) + (width - j - 1)));
                newList.set((width*i) + (width - j - 1), tempBlock);
            }
        }
        //System.out.println("List done being rotated. List size: " + inputList.size());
        return newList;
    }

    public static List<Block> rotateBase(List<Block> inputList){
        List<List<Block>> blockList = parseBase(inputList);
        List<Block> outList = new ArrayList<>();
        for (List<Block> list : blockList){
            outList.addAll(rotateList(list));
        }
        return outList;
    }

    public static MinecraftLocation getBeaconTeleport(BlockPos pos, World world){
        MinecraftLocation beaconLocation = new MinecraftLocation(pos, world.getRegistryKey());
        System.out.println("Received information, attempting beacon warp");
        List<Block> baseBlockList = scanBase(pos, world);
        if (!beaconMap.containsKey(baseBlockList)) {
            System.out.println("Cannot teleport, block list not found!");
            return beaconLocation;
        }
        List<MinecraftLocation> posList = channelMap.get(beaconMap.get(baseBlockList));
        for (int i = 0; i < posList.size(); i++) {
            MinecraftLocation location = posList.get(i);
            if (location.equals(beaconLocation)) {
                System.out.println("Beacon is at index " + i + " of " + posList.size());
                System.out.println("Teleporting...");
                return posList.get((i + 1) % posList.size());
            }
        }
        System.out.println("This beacon is not actually in the warp list! Cancelling teleport.");
        return beaconLocation;
    }

    public static List<List<Block>> parseBase(List<Block> blockList){
        int size = blockList.size();
        if ((size != 9) && (size != 34) && (size != 83) && (size != 164)){
            System.out.println("UNCAUGHT ERROR! Beacon base has invalid length in parseBase!");
        }
        List<List<Block>> outList = new ArrayList<>();
        int beaconLevelLimit = 4;
        for (int i = 1; i <= beaconLevelLimit; i++){
            int startNum = (i*((4*i*i) - 1)) /3 - 1;
            int endNum = ((i+1)*((4*(i+1)*(i+1)) - 1)) /3 - 1;
            if (size > startNum) { //(n*(4*n^2-1)/3)-1 is a mathematical expression for the number of total blocks in a beacon of size n. Thanks, OEIS!
                List<Block> newList = blockList.subList(startNum, endNum);
                outList.add(newList);
            } else
                i = beaconLevelLimit;
        }
        return outList;
    }

    public static void printList(List<Block> listIn){
        int width = (int) Math.sqrt(listIn.size());
        for (int i = 0; i <= width-1; i++){
            String outString = "";
            for (int j = 0; j <= width-1; j++){
                String tempString = listIn.get((width*i) + j).toString();
                outString += tempString.substring(6,tempString.length()-1);
                if (j != width-1)
                    outString += ", ";
            }
            System.out.println(outString);
        }
    }

    public static void printBase(List<List<Block>> baseList){
        System.out.println("Beacon base:");
        for (List<Block> layer : baseList){
            printList(layer);
        }
    }

    public static void printBlockPos(MinecraftLocation location){
        System.out.println(location);
    }

    public static void printFullMap(){
        System.out.println("A full map print has been called. This may take time.");
        System.out.println(" ");
        System.out.println(" ");
        for (Map.Entry<List<Block>, Integer> entry : beaconMap.entrySet()){
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
        for (Map.Entry<Integer, List<MinecraftLocation>> entry : channelMap.entrySet()){
            System.out.println("---------------------------------------");
            System.out.println("Channel " + entry.getKey() + " corresponds to:");
            for (MinecraftLocation location : entry.getValue()){
                printBlockPos(location);
            }
        }
        System.out.println("---------------------------------------");
        System.out.println(" ");
        System.out.println(" ");
        System.out.println("Full print concluded.");
    }

}
