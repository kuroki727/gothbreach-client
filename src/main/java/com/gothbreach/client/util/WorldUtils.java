package com.gothbreach.client.util;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public class WorldUtils {
    public static Block getBlock(BlockPos pos) {
        return MinecraftClient.getInstance().world.getBlockState(pos).getBlock();
    }
    public static boolean isAir(BlockPos pos) {
        return getBlock(pos) == Blocks.AIR;
    }
}	
