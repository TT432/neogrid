package io.github.tt432.neogrid.client;

import io.github.tt432.neogrid.block.entity.ElectricPoleBlockEntityTypes;
import io.github.tt432.neogrid.client.renderer.ElectricPoleBlockEntityRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import static io.github.tt432.neogrid.NeoGrid.ELECTRIC_POLE_BLOCK_ENTITY;

/**
 * @author TT432
 */
@EventBusSubscriber(Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Register renderer for the default pole
        event.registerBlockEntityRenderer(ELECTRIC_POLE_BLOCK_ENTITY.get(), ElectricPoleBlockEntityRenderer::new);

        // Register renderers for all KubeJS-registered pole variants
        for (var type : ElectricPoleBlockEntityTypes.getAllTypes().values()) {
            if (type != ELECTRIC_POLE_BLOCK_ENTITY.get()) {
                event.registerBlockEntityRenderer(type, ElectricPoleBlockEntityRenderer::new);
            }
        }
    }
}
