package in.lakazatong.pcbmod.redstone.blocks;

import in.lakazatong.pcbmod.redstone.Block;
import in.lakazatong.pcbmod.redstone.BlockType;
import in.lakazatong.pcbmod.redstone.Props;
import in.lakazatong.pcbmod.redstone.Structure;

public class Torch extends Delayed {

    public Torch(Structure structure, Props p) {
        super(BlockType.TORCH, structure, p);
        props.delay = 2;
        logicImpl = super::unlockableLogic;
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
    protected void setSignal(long t, Props p) {
        p.signal = powered ? 0 : 15;
    }
}
