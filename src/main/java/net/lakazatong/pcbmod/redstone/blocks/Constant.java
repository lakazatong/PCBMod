package net.lakazatong.pcbmod.redstone.blocks;

import net.lakazatong.pcbmod.redstone.circuit.Block;
import net.lakazatong.pcbmod.redstone.circuit.BlockType;
import net.lakazatong.pcbmod.redstone.circuit.Props;
import net.lakazatong.pcbmod.redstone.circuit.Structure;

public abstract class Constant extends Block {
    public Constant(BlockType type, Structure structure, Props p) {
        super(type, structure, p);
    }

    @Override
    public void logic() {
    }
}
