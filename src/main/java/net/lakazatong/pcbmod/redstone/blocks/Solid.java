package net.lakazatong.pcbmod.redstone.blocks;

import net.lakazatong.pcbmod.redstone.circuit.BlockType;
import net.lakazatong.pcbmod.redstone.circuit.Props;
import net.lakazatong.pcbmod.redstone.circuit.Structure;

public class Solid extends SolidLike {
    public Solid(Structure structure, Props p) {
        super(BlockType.SOLID, structure, p);
    }
}
