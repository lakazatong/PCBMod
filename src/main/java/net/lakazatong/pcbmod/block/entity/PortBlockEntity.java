package net.lakazatong.pcbmod.block.entity;

import net.lakazatong.pcbmod.block.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

public class PortBlockEntity extends BlockEntity {
    private int clicks = 0;

    public PortBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PORT, pos, state);
    }

    public int getClicks() {
        return clicks;
    }

    public void incrementClicks() {
        clicks++;
        markDirty();
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        nbt.putInt("clicks", clicks);

        super.writeNbt(nbt, registries);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);

        clicks = nbt.getInt("clicks");
    }
}