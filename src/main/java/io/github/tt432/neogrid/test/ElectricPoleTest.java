package io.github.tt432.neogrid.test;

import io.github.tt432.neogrid.NeoGrid;
import io.github.tt432.neogrid.block.entity.ElectricPoleBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.testframework.annotation.RegisterStructureTemplate;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.StructureTemplateBuilder;
import net.neoforged.testframework.impl.test.AbstractTest;

import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.energy.EnergyStorage;

import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.testframework.annotation.RegisterStructureTemplate;
import net.neoforged.testframework.gametest.StructureTemplateBuilder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.testframework.annotation.OnInit;
import net.neoforged.testframework.impl.MutableTestFramework;

@TestHolder(value = "pole_transfer_test", description = "Test energy transfer through multiple poles")
public class ElectricPoleTest extends AbstractTest {
    @RegisterStructureTemplate("neogrid:empty_5x5")
    public static final StructureTemplate EMPTY_5X5 = StructureTemplateBuilder.withSize(5, 5, 5).build();

    private static final Map<BlockPos, EnergyStorage> testEnergyMap = new ConcurrentHashMap<>();

    public static EnergyStorage getTestEnergy(BlockPos pos) {
        return testEnergyMap.computeIfAbsent(pos.immutable(), p -> new EnergyStorage(10000));
    }

    @OnInit
    public static void onInit(MutableTestFramework framework) {
        // Since we can't easily get the bus from MutableTestFramework in this version,
        // and @OnInit is too late for some events, let's keep the test-specific registration
        // in NeoGrid but gated by a check or handled differently.
        // Actually, the most reliable way for a GameTest is to register it globally 
        // but only have it active during the test or for a specific test block.
    }

    @Override
    @GameTest(template = "neogrid:empty_5x5")
    protected void onGameTest(GameTestHelper helper) {
        BlockPos posA = new BlockPos(1, 2, 1);
        BlockPos posB = new BlockPos(3, 2, 1);
        BlockPos machineSource = new BlockPos(1, 2, 0);
        BlockPos machineSink = new BlockPos(3, 2, 0);

        helper.setBlock(posA, NeoGrid.ELECTRIC_POLE_BLOCK.get());
        helper.setBlock(posB, NeoGrid.ELECTRIC_POLE_BLOCK.get());
        
        helper.setBlock(machineSource, Blocks.IRON_BLOCK);
        helper.setBlock(machineSink, Blocks.IRON_BLOCK);

        helper.startSequence()
                .thenIdle(25) // Wait for scan
                .thenExecute(() -> {
                    IEnergyStorage source = helper.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, helper.absolutePos(machineSource), null);
                    if (source != null) {
                        source.receiveEnergy(5000, false);
                    }
                })
                .thenWaitUntil(() -> {
                    IEnergyStorage sink = helper.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, helper.absolutePos(machineSink), null);
                    if (sink == null || sink.getEnergyStored() <= 0) {
                        throw new net.minecraft.gametest.framework.GameTestAssertException("Waiting for energy to reach sink machine...");
                    }
                })
                .thenSucceed();
    }
}


