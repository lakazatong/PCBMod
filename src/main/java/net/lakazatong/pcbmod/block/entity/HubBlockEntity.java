package net.lakazatong.pcbmod.block.entity;

import net.lakazatong.pcbmod.block.ModBlockEntities;
import net.lakazatong.pcbmod.block.custom.HubBlock;
import net.lakazatong.pcbmod.block.custom.PortBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

public class HubBlockEntity extends BlockEntity {
    private String structureName = "";
    private int[] portNumbers = new int[6];
    private int[] signals = new int[6];

    public HubBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HUB, pos, state);
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

    public int getPortNumberAt(int side) {
        return portNumbers[side];
    }

    public void setPortNumberAt(int side, int newPortNumber) {
        if (newPortNumber != getPortNumberAt(side)) {
            portNumbers[side] = newPortNumber;
            markDirty();
        }
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
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        nbt.putString("structureName", structureName);
        nbt.putIntArray("portNumbers", portNumbers);
        nbt.putIntArray("signals", signals);

        super.writeNbt(nbt, registries);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);

        structureName = nbt.getString("structureName");
        portNumbers = nbt.getIntArray("portNumbers");
        signals = nbt.getIntArray("signals");
    }
}