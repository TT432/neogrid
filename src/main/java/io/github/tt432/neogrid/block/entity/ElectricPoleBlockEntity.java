package io.github.tt432.neogrid.block.entity;

import io.github.tt432.neogrid.NeoGrid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ElectricPoleBlockEntity extends BlockEntity {
    private int tickCount = 0;
    private final List<BlockPos> connectedBlocks = new ArrayList<>();

    private static final int MAX_TRANSFER_RATE = 1000;

    public ElectricPoleBlockEntity(BlockPos pos, BlockState blockState) {
        super(NeoGrid.ELECTRIC_POLE_BLOCK_ENTITY.get(), pos, blockState);
    }

    public List<BlockPos> getConnectedBlocks() {
        return connectedBlocks;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag connections = new ListTag();
        for (BlockPos pos : connectedBlocks) {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("x", pos.getX());
            posTag.putInt("y", pos.getY());
            posTag.putInt("z", pos.getZ());
            connections.add(posTag);
        }
        tag.put("ConnectedBlocks", connections);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("ConnectedBlocks")) {
            connectedBlocks.clear();
            ListTag connections = tag.getList("ConnectedBlocks", Tag.TAG_COMPOUND);
            for (Tag t : connections) {
                CompoundTag posTag = (CompoundTag) t;
                connectedBlocks.add(new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z")));
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        loadAdditional(tag, lookupProvider);
        if (level != null && level.isClientSide) {
             NeoGrid.LOGGER.info("Client received update for pole at {}: {} connections", worldPosition, connectedBlocks.size());
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        handleUpdateTag(pkt.getTag(), lookupProvider);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ElectricPoleBlockEntity blockEntity) {
        if (level.isClientSide) return;
        
        blockEntity.tickCount++;
        
        // Scan on first tick and every 20 ticks thereafter
        if (blockEntity.tickCount == 1 || blockEntity.tickCount % 20 == 0) {
            scanConnections(level, pos, blockEntity);
        }

        // Transfer energy
        transferEnergy(level, blockEntity);
    }

    private static void scanConnections(Level level, BlockPos pos, ElectricPoleBlockEntity blockEntity) {
        int radius = 10;
        List<BlockPos> newConnections = new ArrayList<>();

        // Optimization: Only scan if level is not null and we are on server side
        if (level == null || level.isClientSide) return;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    
                    BlockPos targetPos = pos.offset(x, y, z);
                    
                    // Simple distance check
                    if (pos.distSqr(targetPos) > radius * radius) continue;

                    // Check if it's another pole
                    BlockEntity targetBE = level.getBlockEntity(targetPos);
                    if (targetBE instanceof ElectricPoleBlockEntity) {
                        newConnections.add(targetPos);
                        continue;
                    }

                    // Check if it's a machine with energy capability
                    boolean hasEnergy = level.getCapability(Capabilities.EnergyStorage.BLOCK, targetPos, null) != null;
                    if (!hasEnergy) {
                        for (Direction dir : Direction.values()) {
                            if (level.getCapability(Capabilities.EnergyStorage.BLOCK, targetPos, dir) != null) {
                                hasEnergy = true;
                                break;
                            }
                        }
                    }

                    if (hasEnergy) {
                        newConnections.add(targetPos);
                    }
                }
            }
        }
        
        if (!newConnections.equals(blockEntity.connectedBlocks)) {
            blockEntity.connectedBlocks.clear();
            blockEntity.connectedBlocks.addAll(newConnections);
            blockEntity.setChanged();
            level.sendBlockUpdated(pos, blockEntity.getBlockState(), blockEntity.getBlockState(), Block.UPDATE_ALL);
        }
    }

    private static void transferEnergy(Level level, ElectricPoleBlockEntity blockEntity) {
        if (blockEntity.connectedBlocks.isEmpty()) return;

        // 1. Find the entire network of poles using BFS to avoid redundant transfers in a mesh
        java.util.Set<BlockPos> networkPoles = new java.util.HashSet<>();
        java.util.Queue<BlockPos> queue = new java.util.LinkedList<>();
        queue.add(blockEntity.worldPosition);
        networkPoles.add(blockEntity.worldPosition);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            BlockEntity be = level.getBlockEntity(current);
            if (be instanceof ElectricPoleBlockEntity pole) {
                for (BlockPos neighbor : pole.connectedBlocks) {
                    if (level.getBlockEntity(neighbor) instanceof ElectricPoleBlockEntity && !networkPoles.contains(neighbor)) {
                        networkPoles.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }

        // 2. Only the "leader" pole (smallest BlockPos) handles the transfer for this tick
        BlockPos leader = networkPoles.stream().min(BlockPos::compareTo).orElse(blockEntity.worldPosition);
        if (!leader.equals(blockEntity.worldPosition)) return;

        // 3. Collect all machines connected to this network
        java.util.Set<BlockPos> networkMachines = new java.util.HashSet<>();
        for (BlockPos polePos : networkPoles) {
            BlockEntity be = level.getBlockEntity(polePos);
            if (be instanceof ElectricPoleBlockEntity pole) {
                for (BlockPos target : pole.connectedBlocks) {
                    if (!(level.getBlockEntity(target) instanceof ElectricPoleBlockEntity)) {
                        networkMachines.add(target);
                    }
                }
            }
        }

        if (networkMachines.isEmpty()) return;

        // 4. Collect all energy handlers from machines
        List<EnergyHandlerAtPos> receivers = new ArrayList<>();
        List<EnergyHandlerAtPos> providers = new ArrayList<>();

        for (BlockPos machinePos : networkMachines) {
            IEnergyStorage nullSide = level.getCapability(Capabilities.EnergyStorage.BLOCK, machinePos, null);
            if (nullSide != null) {
                if (nullSide.canReceive()) receivers.add(new EnergyHandlerAtPos(machinePos, nullSide));
                if (nullSide.canExtract()) providers.add(new EnergyHandlerAtPos(machinePos, nullSide));
            }
            
            for (Direction dir : Direction.values()) {
                IEnergyStorage sideStorage = level.getCapability(Capabilities.EnergyStorage.BLOCK, machinePos, dir);
                if (sideStorage != null) {
                    if (sideStorage.canReceive()) receivers.add(new EnergyHandlerAtPos(machinePos, sideStorage));
                    if (sideStorage.canExtract()) providers.add(new EnergyHandlerAtPos(machinePos, sideStorage));
                }
            }
        }
        
        // Remove duplicates (based on instance)
        receivers = receivers.stream().distinct().toList();
        providers = providers.stream().distinct().toList();

        if (receivers.isEmpty() || providers.isEmpty()) return;

        // 5. Execute direct transfer from providers to receivers
        for (EnergyHandlerAtPos provider : providers) {
            int toExtract = provider.handler.extractEnergy(MAX_TRANSFER_RATE, true);
            if (toExtract <= 0) continue;

            // Distribute energy to all receivers except those at the same position as the provider
            List<EnergyHandlerAtPos> targetReceivers = receivers.stream()
                    .filter(r -> !r.pos.equals(provider.pos))
                    .toList();

            if (targetReceivers.isEmpty()) continue;

            int perReceiver = toExtract / targetReceivers.size();
            if (perReceiver <= 0) perReceiver = toExtract;

            int totalAccepted = 0;
            for (EnergyHandlerAtPos receiver : targetReceivers) {
                int accepted = receiver.handler.receiveEnergy(perReceiver, false);
                if (accepted > 0) {
                    totalAccepted += accepted;
                }
            }

            if (totalAccepted > 0) {
                provider.handler.extractEnergy(totalAccepted, false);
            }
        }
    }

    private record EnergyHandlerAtPos(BlockPos pos, IEnergyStorage handler) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EnergyHandlerAtPos that = (EnergyHandlerAtPos) o;
            return handler == that.handler; 
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(handler);
        }
    }
}
