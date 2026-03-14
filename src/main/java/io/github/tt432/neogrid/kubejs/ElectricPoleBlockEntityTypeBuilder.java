package io.github.tt432.neogrid.kubejs;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import io.github.tt432.neogrid.block.entity.ElectricPoleBlockEntity;
import io.github.tt432.neogrid.block.entity.ElectricPoleBlockEntityTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

/**
 * KubeJS builder that creates and registers a BlockEntityType for a KubeJS electric pole.
 */
public class ElectricPoleBlockEntityTypeBuilder extends BuilderBase<BlockEntityType<?>> {

    private final Supplier<Block> blockSupplier;

    public ElectricPoleBlockEntityTypeBuilder(ResourceLocation id, Supplier<Block> blockSupplier) {
        super(id);
        this.blockSupplier = blockSupplier;
    }

    @Override
    public BlockEntityType<?> createObject() {
        Block block = blockSupplier.get();
        BlockEntityType<ElectricPoleBlockEntity> type = BlockEntityType.Builder.of(
                (pos, state) -> new ElectricPoleBlockEntity(ElectricPoleBlockEntityTypes.getType(block), pos, state),
                block
        ).build(null);
        // Register in the global lookup so ElectricPoleBlock can find it
        ElectricPoleBlockEntityTypes.register(block, type);
        return type;
    }
}
