package in.lakazatong.pcbmod.redstone.blocks;

import in.lakazatong.pcbmod.redstone.Block;
import in.lakazatong.pcbmod.redstone.BlockType;
import in.lakazatong.pcbmod.redstone.Props;
import in.lakazatong.pcbmod.redstone.Structure;

public abstract class Constant extends Block {
    public Constant(BlockType type, Structure structure, Props p) {
        super(type, structure, p);
    }

    @Override
    public void logic(long t) {
    }
}
