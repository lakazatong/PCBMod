package in.lakazatong.pcbmod.redstone.blocks;

import in.lakazatong.pcbmod.redstone.Block;
import in.lakazatong.pcbmod.redstone.BlockType;
import in.lakazatong.pcbmod.redstone.Props;
import in.lakazatong.pcbmod.redstone.Structure;

public class Torch extends Delayed {

    public Torch(Structure structure, Props p) {
        super(BlockType.TORCH, structure, p);
    }

    @Override
    public boolean isInputOf(Block neighbor) {
        return switch (neighbor.type) {
            case SOLID -> neighbor.isAbove(this);
            case DUST -> true;
            case REPEATER -> !neighbor.locked() && neighbor.isFacingAway(this);
            case COMPARATOR -> neighbor.isFacingAway(this);
            default -> false;
        };
    }

    @Override
    public boolean getShouldPowered() {
        return inputs().anyMatch(i -> i.signal() > 0);
    }

    @Override
    protected void setSignal(long t, Props p) {
        p.signal = 0;
    }

    @Override
    protected void clearSignal(long t, Props p) {
        p.signal = 15;
    }
}
