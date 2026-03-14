package io.github.tt432.neogrid.block.entity;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry that tracks BlockEntityType for each ElectricPoleBlock variant.
 * This allows KubeJS-registered poles to have their own BlockEntityType
 * while sharing the same ElectricPoleBlockEntity class.
 */
public class ElectricPoleBlockEntityTypes {

    private static final Map<Block, BlockEntityType<ElectricPoleBlockEntity>> TYPES = new ConcurrentHashMap<>();

    public static void register(Block block, BlockEntityType<ElectricPoleBlockEntity> type) {
        TYPES.put(block, type);
    }

    public static BlockEntityType<ElectricPoleBlockEntity> getType(Block block) {
        return TYPES.get(block);
    }

    public static Map<Block, BlockEntityType<ElectricPoleBlockEntity>> getAllTypes() {
        return TYPES;
    }
}
