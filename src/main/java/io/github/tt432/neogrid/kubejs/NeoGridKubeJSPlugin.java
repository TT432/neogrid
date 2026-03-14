package io.github.tt432.neogrid.kubejs;

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.BuilderTypeRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

/**
 * KubeJS plugin for NeoGrid.
 * <p>
 * Registers the electric_pole block builder type so users can create
 * custom electric poles with different transfer rates and wire colors.
 */
public class NeoGridKubeJSPlugin implements KubeJSPlugin {

    @Override
    public void registerBuilderTypes(BuilderTypeRegistry registry) {
        registry.of(Registries.BLOCK, reg -> {
            reg.add(
                    ResourceLocation.fromNamespaceAndPath("neogrid", "electric_pole"),
                    ElectricPoleBlockBuilder.class,
                    ElectricPoleBlockBuilder::new
            );
        });
    }
}
