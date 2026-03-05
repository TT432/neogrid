package io.github.tt432.neogrid.client;

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
        event.registerBlockEntityRenderer(ELECTRIC_POLE_BLOCK_ENTITY.get(), ElectricPoleBlockEntityRenderer::new);
    }
}
