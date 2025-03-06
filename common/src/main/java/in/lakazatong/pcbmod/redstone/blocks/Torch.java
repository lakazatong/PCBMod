package in.lakazatong.pcbmod.redstone.blocks;

import in.lakazatong.pcbmod.redstone.Block;
import in.lakazatong.pcbmod.redstone.BlockType;
import in.lakazatong.pcbmod.redstone.Props;
import in.lakazatong.pcbmod.redstone.Structure;
import in.lakazatong.pcbmod.utils.Vec3;

public class Torch extends Block {

    public Torch(Vec3 coords, Structure structure) {
        super(BlockType.TORCH, coords, structure);
        props.delay = 2;
    }

    @Override
    public boolean isInputOf(Block neighbor) {
        return switch (neighbor.type) {
            case SOLID -> neighbor.isAbove(this);
            case DUST -> true;
            case REPEATER, COMPARATOR -> neighbor.isFacingAway(this);
            default -> false;
        };
    }

    @Override
    public void logic(double t, Props p) {
        // TODO: consider the 1 redstone tick delay of the torch
        p.signal = 0;
        for (Block block : inputs()) {
            if (block.signal() > 0)
                return;
        }
        p.signal = 15;
    }
}
