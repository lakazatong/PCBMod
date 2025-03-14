package net.lakazatong.pcbmod.block.entity;

import net.lakazatong.pcbmod.block.ModBlockEntities;
import net.lakazatong.pcbmod.block.custom.PortBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

public class PortBlockEntity extends BlockEntity {
    private int portNumber = 0;
    private int signal = 0;

    public PortBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PORT, pos, state);
    }

    public PortBlock.PortType getPortType() {
        return getCachedState().get(PortBlock.TYPE);
    }

    public void setPortType(PortBlock.PortType newType) {
        if (newType != getPortType()) {
            assert world != null;

            world.setBlockState(pos, getCachedState().with(PortBlock.TYPE, newType));
        }
    }

    public int getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(int newPortNumber) {
        if (newPortNumber != portNumber) {
            portNumber = newPortNumber;
            markDirty();
        }
    }

    public int getSignal() {
        return signal;
    }

    public void setSignal(int signal) {
        this.signal = signal;
    }

//    public PortBlock.Side getSide() {
//        return getCachedState().get(PortBlock.SIDE);
//    }
//
//    public void setSide(PortBlock.Side newSide) {
//        if (newSide != getSide()) {
//            assert world != null;
//            world.setBlockState(pos, getCachedState().with(PortBlock.SIDE, newSide));
//        }
//    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        nbt.putInt("portNumber", portNumber);
        nbt.putInt("signal", signal);

        super.writeNbt(nbt, registries);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);

        portNumber = nbt.getInt("portNumber");
        signal = nbt.getInt("signal");
    }
}