package net.lakazatong.pcbmod.redstone.blocks;

import net.lakazatong.pcbmod.redstone.circuit.Block;
import net.lakazatong.pcbmod.redstone.circuit.BlockType;
import net.lakazatong.pcbmod.redstone.circuit.Props;

public abstract class Constant extends Block {
    public Constant(BlockType type, Props p) {
        super(type, p);
    }

    @Override
    public void logic() {
    }
}
