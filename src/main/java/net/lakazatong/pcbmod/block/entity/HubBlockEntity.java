package net.lakazatong.pcbmod.block.entity;

import net.lakazatong.pcbmod.block.ModBlockEntities;
import net.lakazatong.pcbmod.block.custom.HubBlock;
import net.lakazatong.pcbmod.block.custom.PortBlock;
import net.lakazatong.pcbmod.redstone.utils.Direction;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class HubBlockEntity extends BlockEntity {
    private String structureName = "";
    private int instanceId = 0;
    private int[] portNumbers = new int[6];
    private int[] signals = new int[6];

    public HubBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HUB, pos, state);
    }

    public Direction getFacing(BlockState state) {
        return Direction.fromMinecraft(state.get(HubBlock.FACING));
    }

    public PortBlock.PortType getPortTypeAt(int side) {
        return getCachedState().get(HubBlock.SIDE_TYPES[side]);
    }

    public void setPortTypeAt(int side, PortBlock.PortType newType) {
        if (newType != getPortTypeAt(side)) {
            assert world != null;
            world.setBlockState(pos, getCachedState().with(HubBlock.SIDE_TYPES[side], newType));
        }
    }

    public String getStructureName() {
        return structureName;
    }

    public void setStructureName(String newStructureName) {
        if (!newStructureName.equals(structureName)) {
            structureName = newStructureName;
            markDirty();
        }
    }

    public int getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(int newInstanceId) {
        if (newInstanceId != instanceId) {
            instanceId = newInstanceId;
            markDirty();
        }
    }

    public String getCircuitName() {
        return structureName.isEmpty() ? "" : structureName + instanceId;
    }

    public int getPortNumberAt(int side) {
        return portNumbers[side];
    }

    public Integer getSideOf(int portNumber) {
        for (Direction dir : Direction.values()) {
            if (portNumbers[dir.side] == portNumber)
                return dir.side;
        }
        return null;
    }

    public int[] getPortNumbers() {
        return portNumbers;
    }

    public void setPortNumberAt(int side, int newPortNumber) {
        if (newPortNumber != getPortNumberAt(side)) {
            portNumbers[side] = newPortNumber;
            markDirty();
        }
    }

    public void setPortNumbers(int[] portNumbers) {
        this.portNumbers = portNumbers;
    }

    public int getSignalAt(int side) {
        return signals[side];
    }

    public void setSignalAt(int side, int newSignal) {
        if (newSignal != getSignalAt(side)) {
            signals[side] = newSignal;
            markDirty();
        }
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return createNbt(registries);
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        nbt.putString("structureName", structureName);
        nbt.putInt("instanceId", instanceId);
        nbt.putIntArray("portNumbers", portNumbers);
        nbt.putIntArray("signals", signals);

        super.writeNbt(nbt, registries);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);

        structureName = nbt.getString("structureName");
        instanceId = nbt.getInt("instanceId");
        portNumbers = nbt.getIntArray("portNumbers");
        signals = nbt.getIntArray("signals");
    }
}