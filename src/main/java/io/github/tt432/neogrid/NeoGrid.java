package io.github.tt432.neogrid;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;
import io.github.tt432.neogrid.block.ElectricPoleBlock;
import io.github.tt432.neogrid.block.entity.ElectricPoleBlockEntity;
import io.github.tt432.neogrid.client.ClientModEvents;
import io.github.tt432.neogrid.client.renderer.ElectricPoleBlockEntityRenderer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.testframework.annotation.OnInit;
import net.neoforged.testframework.conf.ClientConfiguration;
import net.neoforged.testframework.conf.FrameworkConfiguration;
import net.neoforged.testframework.impl.MutableTestFramework;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(NeoGrid.MOD_ID)
public class NeoGrid {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "neogrid";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "neogrid" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    // Create a Deferred Register to hold Items which will all be registered under the "neogrid" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "neogrid" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    // Create a Deferred Register to hold BlockEntities which will all be registered under the "neogrid" namespace
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);

    public static final DeferredBlock<ElectricPoleBlock> ELECTRIC_POLE_BLOCK = BLOCKS.register("electric_pole_block", () -> new ElectricPoleBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).noOcclusion().strength(2.0f)));
    public static final DeferredItem<BlockItem> ELECTRIC_POLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("electric_pole_block", ELECTRIC_POLE_BLOCK);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ElectricPoleBlockEntity>> ELECTRIC_POLE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("electric_pole_block_entity", () -> BlockEntityType.Builder.of(ElectricPoleBlockEntity::new, ELECTRIC_POLE_BLOCK.get()).build(null));

    // Creates a creative tab with the id "neogrid:example_tab" for the example item, that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("neogrid_group", () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.neogrid")).icon(() -> ELECTRIC_POLE_BLOCK_ITEM.get().getDefaultInstance()).displayItems((parameters, output) -> {
        output.accept(ELECTRIC_POLE_BLOCK_ITEM.get());
    }).build());

    public NeoGrid(IEventBus modEventBus, ModContainer modContainer) {
        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so block entities get registered
        BLOCK_ENTITY_TYPES.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (NeoGrid) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::registerCapabilities);

        final MutableTestFramework framework = FrameworkConfiguration.builder(ResourceLocation.fromNamespaceAndPath(MOD_ID, "tests"))
                .clientConfiguration(() -> ClientConfiguration.builder()
                        .toggleOverlayKey(GLFW.GLFW_KEY_J)
                        .openManagerKey(GLFW.GLFW_KEY_N)
                        .build())
                .build().create();

        framework.init(modEventBus, modContainer);

        NeoForge.EVENT_BUS.addListener((final RegisterCommandsEvent event) -> {
            final LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal("tests");
            framework.registerCommands(node);
            event.getDispatcher().register(node);
        });

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.register(ClientModEvents.class);
        }
    }

    @OnInit
    public static void onFrameworkInit(MutableTestFramework framework) {
        LOGGER.info("Test Framework Initialized for NeoGrid");
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        // Electric Pole no longer has internal storage

        // Mock energy storage for IRON_BLOCK, used only in ElectricPoleTest
        event.registerBlock(Capabilities.EnergyStorage.BLOCK, (level, pos, state, be, context) -> {
            if (level.isClientSide) return null;
            try {
                // Use reflection or just direct access if possible
                return io.github.tt432.neogrid.test.ElectricPoleTest.getTestEnergy(pos);
            } catch (NoClassDefFoundError | Exception e) {
                return null;
            }
        }, Blocks.IRON_BLOCK);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}
