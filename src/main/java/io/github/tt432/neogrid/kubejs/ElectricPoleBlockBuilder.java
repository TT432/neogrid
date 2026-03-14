package io.github.tt432.neogrid.kubejs;

import dev.latvian.mods.kubejs.block.BlockBuilder;
import dev.latvian.mods.kubejs.registry.AdditionalObjectRegistry;
import io.github.tt432.neogrid.block.ElectricPoleBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;

/**
 * KubeJS BlockBuilder for creating custom Electric Pole blocks.
 * <p>
 * Allows configuring transfer rate and wire color via KubeJS scripts.
 */
public class ElectricPoleBlockBuilder extends BlockBuilder {

    private int maxTransferRate = 1000;
    private int connectionRange = 10;
    private float wireColorR = 0.0F;
    private float wireColorG = 0.5F;
    private float wireColorB = 1.0F;

    public ElectricPoleBlockBuilder(ResourceLocation id) {
        super(id);
        // Default properties for electric poles
        this.opaque(false);
        this.hardness(2.0F);
        this.resistance(2.0F);
        this.mapColor(MapColor.METAL);
    }

    /**
     * Sets the maximum energy transfer rate (FE/tick) for this pole.
     */
    public ElectricPoleBlockBuilder transferRate(int rate) {
        this.maxTransferRate = rate;
        return this;
    }

    /**
     * Sets the auto-connection range (in blocks) for this pole. Default is 10.
     */
    public ElectricPoleBlockBuilder connectionRange(int range) {
        this.connectionRange = range;
        return this;
    }

    /**
     * Sets the wire color using RGB float values (0.0 ~ 1.0).
     */
    public ElectricPoleBlockBuilder wireColor(float r, float g, float b) {
        this.wireColorR = r;
        this.wireColorG = g;
        this.wireColorB = b;
        return this;
    }

    /**
     * Sets the wire color using a hex color value (e.g. 0xFF0000 for red).
     */
    public ElectricPoleBlockBuilder wireColorHex(int hex) {
        this.wireColorR = ((hex >> 16) & 0xFF) / 255.0F;
        this.wireColorG = ((hex >> 8) & 0xFF) / 255.0F;
        this.wireColorB = (hex & 0xFF) / 255.0F;
        return this;
    }

    public int getMaxTransferRate() {
        return maxTransferRate;
    }

    public float getWireColorR() {
        return wireColorR;
    }

    public float getWireColorG() {
        return wireColorG;
    }

    public float getWireColorB() {
        return wireColorB;
    }

    @Override
    public Block createObject() {
        return new ElectricPoleBlock(
                createProperties(),
                maxTransferRate,
                connectionRange,
                wireColorR,
                wireColorG,
                wireColorB
        );
    }

    @Override
    public void createAdditionalObjects(AdditionalObjectRegistry registry) {
        super.createAdditionalObjects(registry);
        // Register a BlockEntityType for this pole variant
        registry.add(
                Registries.BLOCK_ENTITY_TYPE,
                new ElectricPoleBlockEntityTypeBuilder(
                        ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "_entity"),
                        this
                )
        );
    }
}
