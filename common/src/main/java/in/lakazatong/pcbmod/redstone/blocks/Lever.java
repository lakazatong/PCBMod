package in.lakazatong.pcbmod.redstone.blocks;

import in.lakazatong.pcbmod.redstone.Block;
import in.lakazatong.pcbmod.redstone.BlockType;
import in.lakazatong.pcbmod.redstone.Props;
import in.lakazatong.pcbmod.redstone.Structure;

public class Lever extends Constant {
    public Lever(Structure structure, Props p) {
        super(BlockType.LEVER, structure, p);
    }

    @Override
    public boolean isInputOf(Block neighbor) {
        return switch (neighbor.type) {
            case AIR -> false;
            case SOLID -> isFacingAway(neighbor);
            case DUST -> true;
            case TORCH -> false;
            case REPEATER, COMPARATOR -> neighbor.isFacingAway(this);
            case BUTTON -> false;
            case LEVER -> false;
            case REDSTONE_BLOCK -> false;
        };
    }
}
